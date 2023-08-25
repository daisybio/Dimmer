require(lme4)
require(car)
require(data.table)
require(BiocParallel)


# Main function of script: read files and calculate linear mixed model for all CpGs in provided beta matrix
# @param beta_matrix_file file path to beta matrix; has to be written to file with the WriteBetaMatrix() function from Dimmer
# @param variable_index the variable index of interest for which pvalues will be extracted (based on annotation file)
# @param sample_order_file file path to sample index order file; will be used to re-order the columns of beta matrix according
#   to the shuffling that happens during permutations; if original p-values are calculated in Dimmer, this has no effect, since
#   the samples were not shuffled
# @param mm_pvalues_file output file where mixed model p-values will be stored
# @param formula formula for mixed model; name of dependent variable (before ~) has to be 'beta_value'; other variables have to
#   be present in the annotation file
# @param annotation_file path to Dimmer annotation file
# @param variance_cutoff CpGs with a variance below this value are not considered for a mixed model and skipped; a p-value of 0.99 
#   will be returned
# @param ncores number of cores that are used for parallel mixed model calculation
runModel <- function(beta_matrix_file, variable_index, sample_order_file, mm_pvalues_file, formula, annotation_file, variance_cutoff, ncores) {

    # read data
    formula <- as.formula(formula)
    annotation_data <- data.table::fread(annotation_file)
    sample_order <- data.table::fread(sample_order_file)[[1]]+1 # +1 to account for Javas 0-based indexing
    beta_matrix <- data.table::fread(beta_matrix_file)[,-1]

    variable <- colnames(annotation_data)[variable_index]
    # re-order variable of interest in annotation data to have same order as in (possible) permutation
    # if no permutation has been done in Java, this will just result in an unchanged annotation_data table
    annotation_data[[variable]] <- annotation_data[[variable]][sample_order]

    # parallel execution of linear mixed model for every CpG in beta matrix
    pvalues <- unlist(parallel::mclapply(1:nrow(beta_matrix), function(i){
        beta_cpg <- as.numeric(beta_matrix[i])

        if(var(beta_cpg, na.rm = T) > as.numeric(variance_cutoff)){
            mapping_tmp <- annotation_data
            mapping_tmp$beta_value <- beta_cpg

            model <- suppressMessages(lme4::lmer(formula, data=mapping_tmp))
            a <- suppressMessages(car::Anova(model))
            return(a$`Pr(>Chisq)`[which(row.names(a) == variable)])
        }else{
            return(0.99)
        }
    }, mc.cores = as.numeric(ncores)))
  
    save_model_information(pvalues, mm_pvalues_file)
}

#Saves necessary information of the mixed Model
#@param pvalues list of pvalues from mixed model
#@param outputPath Path of folder, in which the data should be saved.
save_model_information <- function(pvalues, outputPath) {
	write.table(pvalues, file=outputPath, row.names=FALSE, col.names=FALSE, sep=",")
}

# Checks if the arguments are complete and correct
# If not quits with the error-code
# 2, if there aren't enough parameters.
# 3, if the formula is not valid.
# 4, if the beta matrix file does not exist.
# 5, if the annotation file does not exist.
# 6, if the sample order file does not exist.
# 7, cutoff is not between 0 and 1.

#@return args the arguments
checkArgs <- function() {
  args = commandArgs(trailingOnly=TRUE)
  if (length(args) != 8) {
    quit(status=2, save = 'no')
  } else if (!checkFormula(args[5])) {
    quit(status=3, save = 'no')
  } else if (!file.exists(args[1])) {
    quit(status=4, save = 'no')
  } else if (!file.exists(args[6])) {
    quit(status=5, save = 'no')
  } else if (!file.exists(args[3])) {
    quit(status=6, save = 'no')
  } else if (as.numeric(args[7])<0 | as.numeric(args[7])>1){
    print(args[7])
    quit(status = 7, save = 'no')
  }
  return (args)
}


#Check if the formula is valid
#@return TRUE if it is valid
#@reutrn FALSE if an error occured, which means that the formula is not valid
checkFormula <- function(formu) {
	tryCatch(
			{
				as.formula(formu)
				return(TRUE)
			},
			error=function(cond){
				return (FALSE)
			}
	)
}

args <- checkArgs()
beta_matrix_file <- args[1]
variable_index <- args[2]
sample_order_file <- args[3]
mm_pvalues_file <- args[4]
formula <- args[5]
annotation_file <- args[6]
variance_cutoff <- args[7]
ncores <- args[8]

options(error=function() traceback(2))

runModel(beta_matrix_file = beta_matrix_file,
         variable_index = variable_index,
         sample_order_file = sample_order_file,
         mm_pvalues_file = mm_pvalues_file,
         formula = formula,
         annotation_file = annotation_file,
         variance_cutoff =  variance_cutoff,
         ncores = ncores)
