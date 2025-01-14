### Load libraries ####
require(data.table)
require(dplyr)
require(stats)
require(BiocParallel)


# Main function of script: read files and calculate the given model for all CpGs in provided beta matrix
# @param beta_matrix_file file path to beta matrix; has to be written to file with the WriteBetaMatrix() function from Dimmer
# @param variable_index the variable index of interest for which pvalues will be extracted (based on annotation file)
# @param sample_order_file file path to sample index order file; reorders the samples in case of permutations
# @param pvalues_file output file where model p-values will be stored
# @param formula formula for the model; name of dependent variable (before ~) has to be 'beta_value'; other variables
#   have to be present in the annotation file (this has been checked by Dimmer)
# @param annotation_file path to Dimmer annotation file
# @param variance_cutoff CpGs with a variance below this value are not considered for the model and skipped;
#   a p-value of 0.99 will be returned (this only happens for permutations, Dimmer sets variance_cutoff to 0.0 for
#   original p-value calculations)
# @param ncores number of cores that are used for parallel time series model calculation
# @param method name the model that is to be used


### Functions ####
runModel <-
  function(beta_matrix_file,
           variable_index,
           sample_order_file,
           pvalues_file,
           formula,
           annotation_file,
           variance_cutoff,
           ncores,
           method) {
    # read data
    formula <- as.formula(formula)
    annotation_data <- data.table::fread(annotation_file)
    sample_order <-
      data.table::fread(sample_order_file)[[1]] + 1 #account for Javas 0-based indexing
    beta_matrix <- data.table::fread(beta_matrix_file)[, -1]
    
    # print the used variance cutoff (used for testing if variance cutoff for original p-value calculation is indeed
    # set to 0.0 by Dimmer; is retained for information and clarity)
    cat(
      paste(
        "\nThe variance_cutoff for the following calculations is",
        variance_cutoff,
        "\t"
      )
    )

    # distinguish which model has been selected and apply it
    if (method == "friedmanT") {
      pvalues <-
        execute_friedman(beta_matrix,
                         annotation_data,
                         formula,
                         sample_order,
                         variance_cutoff,
                         ncores)
    } else if (method == "rmANOVA") {
      pvalues <-
        execute_anova(beta_matrix,
                      annotation_data,
                      formula,
                      sample_order,
                      variance_cutoff,
                      ncores)
    } else if (method == "mixedModel") {
      pvalues <-
        execute_mixedModel(
          beta_matrix,
          annotation_data,
          formula,
          variable_index,
          sample_order,
          variance_cutoff,
          ncores
        )
    }
    
    save_model_information(pvalues, pvalues_file)
  }

execute_friedman <-
  function(beta_matrix,
           annotation_data,
           formula,
           sample_order,
           variance_cutoff,
           ncores) {
    indep_vars = all.vars(formula[-2])
    timestamp = indep_vars[1]
    annotation_data[, (indep_vars) := lapply(.SD, FUN = as.factor), .SDcols = indep_vars]
    
    pvalues <-
      unlist(parallel::mclapply(
        1:nrow(beta_matrix),
        FUN = function(i) {
          beta_cpg <- as.numeric(beta_matrix[i])
          # if variance of row bigger than cutoff calculate method
          if (var(beta_cpg, na.rm = T) > as.numeric(variance_cutoff)) {
            mapping_tmp <- annotation_data
            mapping_tmp$beta_value <- beta_cpg
            
            # order beta values based on the (potential) shuffeling of samples in Dimmer
            # only has an effect if permutation pvalues are calculated
            mapping_tmp[['beta_value']] <-
              mapping_tmp[['beta_value']][sample_order]
            
            # imputation process only if NaN values present
            if (sum(is.na(beta_cpg)) > 0) {
              mapping_tmp <- imputation(mapping_tmp, timestamp)
              # If no imputation (more that 1 value per timestamp missing) or despite (some) imputation still NaN
              # values in the data -> return 0.99
              if (sum(is.na(mapping_tmp$beta_value)) > 0) {
                return(0.99)
              }
            }
            
            model <-
              suppressMessages(friedman.test(formula, data = mapping_tmp))
            return(model$p.value)
            # else p-value of 0.99
          } else{
            return(0.99)
          }
        },
        mc.cores = as.numeric(ncores)
      ))
    
    return(pvalues)
  }

execute_anova <-
  function(beta_matrix,
           annotation_data,
           formula,
           sample_order,
           variance_cutoff,
           ncores) {
    indep_vars = all.vars(formula[-2])
    timestamp = indep_vars[1]
    annotation_data[, (indep_vars) := lapply(.SD, FUN = as.factor), .SDcols = indep_vars]
    
    pvalues <-
      unlist(parallel::mclapply(
        1:nrow(beta_matrix),
        FUN = function(i) {
          beta_cpg <- as.numeric(beta_matrix[i])
          # if variance of row bigger than cutoff calculate method
          if (var(beta_cpg, na.rm = T) > as.numeric(variance_cutoff)) {
            mapping_tmp <- annotation_data
            mapping_tmp$beta_value <- beta_cpg
            
            # order beta values based on the (potential) shuffeling of samples in Dimmer
            # only has an effect if permutation pvalues are calculated
            mapping_tmp[['beta_value']] <-
              mapping_tmp[['beta_value']][sample_order]
            
            # imputation process only if NaN values present
            if (sum(is.na(beta_cpg)) > 0) {
              mapping_tmp <- imputation(mapping_tmp, timestamp)
              # If no imputation (more that 1 value per timestamp missing) or despite (some) imputation still NaN
              # values in the data -> return 0.99
              if (sum(is.na(mapping_tmp$beta_value)) > 0) {
                return(0.99)
              }
            }
            
            model <-
              suppressMessages(aov(formula, data = mapping_tmp))
            return(summary(model)[[2]][[1]]$`Pr(>F)`[1])
            # else p-value of 0.99
          } else{
            return(0.99)
          }
        },
        mc.cores = as.numeric(ncores)
      ))
    
    return(pvalues)
  }

execute_mixedModel <-
  function(beta_matrix,
           annotation_data,
           formula,
           variable_index,
           sample_order,
           variance_cutoff,
           ncores) {
    indep_vars = all.vars(formula[-2])
    annotation_data[, (indep_vars) := lapply(.SD, FUN = as.factor), .SDcols = indep_vars]
    variable <- colnames(annotation_data)[variable_index]
    
    pvalues <-
      unlist(parallel::mclapply(1:nrow(beta_matrix), function(i) {
        beta_cpg <- as.numeric(beta_matrix[i])

        if (var(beta_cpg, na.rm = T) > as.numeric(variance_cutoff)) {
          mapping_tmp <- annotation_data
          mapping_tmp$beta_value <- beta_cpg
          
          # order beta values based on the (potential) shuffeling of samples in Dimmer
          # only has an effect if permutation pvalues are calculated
          mapping_tmp[['beta_value']] <-
            mapping_tmp[['beta_value']][sample_order]
          
          # imputation process only if NaN values present
          if (sum(is.na(beta_cpg)) > 0) {
            mapping_tmp <- imputation(mapping_tmp, variable)
            # If no imputation (more that 1 value per timestamp missing) or despite (some) imputation still NaN
            # values in the data -> return 0.99
            if (sum(is.na(mapping_tmp$beta_value)) > 0) {
              return(0.99)
            }
          }
          
          model <-
            suppressMessages(lme4::lmer(formula, data = mapping_tmp))
          a <- suppressMessages(car::Anova(model))
          pval <- a$`Pr(>Chisq)`[which(row.names(a) == variable)]
          #cat(paste0(i,': ',pval,'; ', a$`Pr(>Chisq)`, '\n'))
          return(a$`Pr(>Chisq)`)
        } else{
          return(0.99)
        }
      }, mc.cores = as.numeric(ncores)))
    
    return(pvalues)
  }

#Saves necessary information of the model
#@param pvalues list of pvalues
#@param outputPath Path of folder, in which the data should be saved.
save_model_information <- function(pvalues, outputPath) {
  write.table(
    pvalues,
    file = outputPath,
    row.names = FALSE,
    col.names = FALSE,
    sep = ","
  )
}

#imputation for beta_values of CpGs (with mean value) iff only one value per timestamp grouping is missing
#@param mapping_tmp data table construct of beta values of one CpG and annotation
#@param timestamp variable name out of formula used for grouping
imputation <- function(mapping_tmp, timestamp) {
  new <- mapping_tmp %>%
    dplyr::group_by_at(timestamp) %>%
    dplyr::mutate_at(
    .tbl = .,
      .vars = vars('beta_value'),
      .funs = function(x) {
        if (sum(is.na(x)) == 1) {
          # imputation of Nan only if it is the only per this timestamp
          mean_x <- mean(x, na.rm = TRUE)
          x[is.na(x)] <- mean_x
        }
        return(x)
      }
    )

  return(new)
}

#Checks if the arguments are complete and correct
#If not quits with the error-code
#2, if there aren't enough or too much parameters.
#3, if the formula is not valid.
#4, if the beta matrix file does not exist.
#5, if the sample order file does not exist.
#6, if the annotation file does not exist.
#7, if the method is neither "friedmanT" or "rmANOVA"

#@return args the arguments
checkArgs <- function() {
  args = commandArgs(trailingOnly = TRUE)
  if (length(args) != 9) {
    quit(status = 2)
  } else if (!checkFormula(args[5])) {
    quit(status = 3)
  } else if (!file.exists(args[1])) {
    quit(status = 4)
  } else if (!file.exists(args[2])) {
    quit(status = 5)
  } else if (!file.exists(args[4])) {
    quit(status = 6)
  } else if (!args[8] %in% c("friedmanT", "rmANOVA", "mixedModel")) {
    quit(status = 7)
  }
  return (args)
}

#Check if the formula is valid
#@return TRUE if it is valid
#@reutrn FALSE if an error occured, which means that the formula is not valid
checkFormula <- function(formu) {
  tryCatch({
    as.formula(formu)
    return(TRUE)
  },
  error = function(cond) {
    return (FALSE)
  })
}


args <- checkArgs()
beta_matrix_file <- args[1]
sample_order_file <- args[2]
pvalues_file <- args[3]
annotation_file <- args[4]
formula <- args[5]
variable_index <- args[6]
variance_cutoff <- args[7]
method <- args[8]
ncores <- args[9]

options(
  error = function()
    traceback(2)
)

runModel(
  beta_matrix_file = beta_matrix_file,
  sample_order_file = sample_order_file,
  pvalues_file = pvalues_file,
  variable_index = variable_index,
  formula = formula,
  annotation_file = annotation_file,
  variance_cutoff =  variance_cutoff,
  method = method,
  ncores = ncores
)
