require(lme4)
require(car)
require(data.table)
require(parallel)


# Main function of script: read files, re-order samples (in case their indices were shuffled during permutation)
# and calculate linear mixed model for all CpGs in provided beta matrix
# @param beta_matrix_file file path to beta matrix; has to be written to file with the WriteBetaMatrix() function from Dimmer
# @param sample_order_file file path to sample index order file; will be used to re-order the columns of beta matrix according
#   to the shuffling that happens during permutations; if original p-values are calculated in Dimmer, this has no effect, since
#   the samples were not shuffled
# @param mm_pvalues_file output file where mixed model p-values will be stored
# @param formula formula for mixed model; name of dependent variable (before ~) has to be 'beta_value'; other variables have to
#   be present in the annotation file
# @param annotation_file path to Dimmer annotation file
# @param ncores number of cores that are used for parallel mixed model calculation
runModel <- function(beta_matrix_file, sample_order_file, mm_pvalues_file, formula, annotation_file, ncores) {

    # read data
    formula <- as.formula(formula)
    annotation_data <- data.table::fread(annotation_file)
    sample_order <- data.table::fread(sample_order_file)[[1]]+1
    beta_matrix <- data.table::fread(beta_matrix_file)[,-1]

    # re-order annotation data frame
    annotation_data <- annotation_data[sample_order,]   # row wise re-ordering

    # parallel execution of linear mixed model for every CpG in beta matrix
    pvalues <- unlist(parallel::mclapply(1:nrow(beta_matrix), function(i){
        beta_cpg <- as.matrix(beta_matrix[i])[1,]
        if(var(beta_cpg, na.rm = T) > .05){
            mapping_tmp <- annotation_data
            mapping_tmp$beta_value <- t(beta_cpg)

            model <- lme4::lmer(formula, data=mapping_tmp)
            a <- car::Anova(model)
            return(a$`Pr(>Chisq)`)
        }else{
            return(0.99)
        }
    }, mc.cores = ncores))

    save_model_information(pvalues, mm_pvalues_file)
}

#Saves necessary information of the mixed Model
#@param pvalues list of pvalues from mixed model
#@param outputPath Path of folder, in which the data should be saved.
save_model_information <- function(pvalues, outputPath) {
	write.table(pvalues, file=outputPath, row.names=FALSE, col.names=FALSE, sep=",")
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
	args = commandArgs(trailingOnly=TRUE)
	if (length(args) != 6) {
		quit(status=2)
	} else if (!checkFormula(args[4])) {
		quit(status=3)
	} else if (!file.exists(args[1])) {
		quit(status=4)
	} else if (!file.exists(args[5])) {
		quit(status=5)
	} else if (!file.exists(args[2])) {
		quit(status=6)
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
sample_order_file <- args[2]
mm_pvalues_file <- args[3]
formula <- args[4]
annotation_file <- args[5]
ncores <- args[6]

options(error=function() traceback(2))

runModel(beta_matrix_file = beta_matrix_file,
         sample_order_file = sample_order_file,
         mm_pvalues_file = mm_pvalues_file,
         formula = formula,
         annotation_file = annotation_file,
         ncores = ncores)



