### Friedman Test R Script description ####
# Based on "Dimmer/src/main/resources/mixed_model.R" script by Alexander Dietrich

# Main function of script: read files, re-order samples (in case their indices were shuffled during permutation)
# and calculate Friedman Test for all CpGs in provided beta matrix
# @param beta_matrix_file file path to beta matrix; has to be written to file with the WriteBetaMatrix() function from Dimmer
# @param sample_order_file file path to sample index order file; will be used to re-order the columns of beta matrix according
#   to the shuffling that happens during permutations; if original p-values are calculated in Dimmer, this has no effect, since
#   the samples were not shuffled
# @param ts_pvalues_file output file where mixed model p-values will be stored
# @param formula formula for mixed model; name of dependent variable (before ~) has to be 'beta_value'; other variables have to
#   be present in the annotation file
# @param annotation_file path to Dimmer annotation file
# @param variance_cutoff CpGs with a variance below this value are not considered for a mixed model and skipped; a p-value of 0.99
#   will be returned
# @param ncores number of cores that are used for parallel mixed model calculation


### Install packages, load libraries ####
require(data.table)
require(stats) # aov()
require(BiocParallel) # parallel::mclapply()
require(dplyr)
#require(plyr) # as.factor()

### Functions ####
runModel <-
  function(beta_matrix_file,
           sample_order_file,
           ts_pvalues_file,
           formula,
           annotation_file,
           variance_cutoff,
           ncores,
           method) {
    # read data
    formula <- as.formula(formula)
    indep_vars = all.vars(formula[-2])
    # for imputation
    # TODO The imputation variable must come at first position
    timestamp = indep_vars[1]
    annotation_data <- data.table::fread(annotation_file)
    # convert all columns of independamt variables with as.factor() (necessary for all categorical columns fro rm anova) 
    # function found on: https://stackoverflow.com/questions/16846380/apply-a-function-to-every-specified-column-in-a-data-table-and-update-by-referen
    annotation_data[ , (indep_vars) := lapply(.SD, FUN=as.factor), .SDcols = indep_vars]
    # | Alternative: user has to specify it in the formula
    
    sample_order <- data.table::fread(sample_order_file)[[1]] + 1
    beta_matrix <- data.table::fread(beta_matrix_file)[, -1]
    
    # re-order annotation data frame
    annotation_data <-
      annotation_data[sample_order, ]   # row wise re-ordering

    cat(paste("\nThe variance_cutoff for the following calculations is",variance_cutoff,"\t"))
    
    if (method == "friedmanT") {
      # parallel execution of the friedman test for every CpG in beta matrix
      pvalues <-
        unlist(parallel::mclapply(
          1:nrow(beta_matrix),
          FUN = function(i,
                         counter_setNANCpGSign,
                         counter_setLowVarCpGSign) {
            beta_cpg <- as.numeric(beta_matrix[i])
            # Test if Remainder of Row number divided by 10000 is 0 => Progress bar?
            if (i %% 10000 == 0) {
              cat(i)
            }
            # if variance of row bigger than cutoff calculate method
            if (var(beta_cpg, na.rm = T) > as.numeric(variance_cutoff)) {
              mapping_tmp <- annotation_data
              mapping_tmp$beta_value <- beta_cpg
              # Imputation process only it NaN values present
              if (sum(is.na(beta_cpg)) > 0) {
                #TODO if imputation and could include cutoff for max missing values per timestamp
                mapping_tmp <- imputation(mapping_tmp, timestamp)
                # If no Imputation or Imputation not enough return 0.99
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
    } else if (method == "rmANOVA") {
      pvalues <-
        unlist(parallel::mclapply(1:nrow(beta_matrix), function(i,
                                                                counter_setNANCpGSign,
                                                                counter_setLowVarCpGSign) {
          beta_cpg <- as.numeric(beta_matrix[i])
          # Test if Remainder of Row number divided by 10000 is 0 => Progress bar?
          if (i %% 10000 == 0) {
            cat(i)
          }
          # if variance of row bigger than cutoff calculate method
          if (var(beta_cpg, na.rm = T) > as.numeric(variance_cutoff)) {
            mapping_tmp <- annotation_data
            mapping_tmp$beta_value <- beta_cpg
            # Imputation process only it NaN values present
            if (sum(is.na(beta_cpg)) > 0) {
              #TODO if imputation and could include cutoff for max missing values per timestamp
              mapping_tmp <- imputation(mapping_tmp, timestamp)
              # If no Imputation or Imputation not enough return 0.99
              if (sum(is.na(mapping_tmp$beta_value)) > 0) {
                return(0.99)
              }
            }
            
            model <- suppressMessages(aov(formula, data = mapping_tmp))
            return(summary(model)[[2]][[1]]$`Pr(>F)`[1]) # TODO: Check again for better/robuster extraction
            # else p-value of 0.99
          } else{
            return(0.99)
          }
        }, mc.cores = as.numeric(ncores)))
    }
    save_model_information(pvalues, ts_pvalues_file)
  }

#Saves necessary information of the mixed Model
#@param pvalues list of pvalues from mixed model
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

#Imputation for beta_values of CpGs (with mean value) iff only one value per timestamp grouping is missing
#@param mapping_tmp data table construct of beta values of one CpG and annotation
#@param timestamp variable name out of formula for the independent variable of the repeated measurements
imputation <- function(mapping_tmp, timestamp) {
  mapping_tmp %>%
    group_by_at(timestamp) %>%
    mutate_at(
      .vars = vars(beta_value),
      .funs = function(x) {
        if (sum(is.na(x)) == 1) {
          # Imputation of Nan only if it is the only per this timestamp
          mean_x <- mean(x, na.rm = TRUE)
          x[is.na(x)] <- mean_x
        }
        return(x)
      }
    )
}

#Checks if the arguments are complete and correct
#If not quits with the error-code
#2, if there aren't enough parameters.
#3, if the formula is not valid.
#4, if the beta matrix file does not exist.
#5, if the annotation file does not exist.
#6, if the sample order file does not exist.

#@return args the arguments
checkArgs <- function() {
  args = commandArgs(trailingOnly = TRUE)
  if (length(args) != 8) {
    quit(status = 2)
  } else if (!checkFormula(args[4])) {
    quit(status = 3)
  } else if (!file.exists(args[1])) {
    quit(status = 4)
  } else if (!file.exists(args[5])) {
    quit(status = 5)
  } else if (!file.exists(args[2])) {
    quit(status = 6)
  } else if (args[8] != "friedmanT" && args[8] != "rmANOVA") {
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


### Script ####
args <- checkArgs()
beta_matrix_file <- args[1]
sample_order_file <- args[2]
ts_pvalues_file <- args[3]
formula <- args[4]
annotation_file <- args[5]
variance_cutoff <- args[6]
ncores <- args[7]
method <- args[8]

options(
  error = function()
    traceback(2)
)


runModel(
  beta_matrix_file = beta_matrix_file,
  sample_order_file = sample_order_file,
  ts_pvalues_file = ts_pvalues_file,
  formula = formula,
  annotation_file = annotation_file,
  variance_cutoff =  variance_cutoff,
  ncores = ncores,
  method = method
)