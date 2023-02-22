#Reads data and performs a mixed Model on them.
#@param inputPath Path to the data file.
#@param formu formula, on which the mixed Model calculates.
#@return m mixed Model
mixed_model <- function (inputPath, formu) {
  	library(lme4)
  	methylData <- read.csv(inputPath, header=TRUE)
	
 	m <- lme4::lmer(as.formula(formu), data=methylData)

	# the following part can be used to calculate p-values with the LMM:
	# source: https://ase.tufts.edu/bugs/guide/assets/mixed_model_guide.html

	# library(car)
	# a <- car::Anova(m)
	# pvalue <- a$`Pr(>Chisq)`
	# return(pvalue)

	return(m)
}


#Saves necessary information of the mixed Model
#@param model mixed Model
#@param outputPath Path of folder, in which the data should be saved.
save_model_information <- function(model, outputPath) {
	#res = model@resp$mu
	#res <- as.list(res)
	para = model@beta
	#res <- append(res, para)
	stdErr = summary(model)$coef[, 2, drop = FALSE]
	test <- append(para, stdErr)

	#print (test)
	#names(frame)[ncol(frame)] <-region	
	#print(frame)
	#print(res)
	library(Matrix)
	write.table(matrix(test, nrow=1), outputPath, sep=", ", row.names=FALSE, col.names=FALSE)
}

#Used for testing
#TODO delete
test <- function() {
	args[1] = "C:/Users/msant/Downloads/Dimmer/src/main/java/dk/sdu/imada/mixed_model/CSV_FILE_NAME.csv"
	args[2] = "C:/Users/msant/Downloads/Dimmer/src/main/java/dk/sdu/imada/mixed_model/results.csv"
	args[3] = "Reaction ~ Days + (Days|Subject)"
}

#Checks if the arguments are complete and correct
#If not quits with the error-code
#2, if there aren,t enough parameters.
#3, if the input file does not exists.
#4, if the formula is not valid.
#@return args the arguments
checkArgs <- function() {
	args = commandArgs(trailingOnly=TRUE)
	if (length(args) < 3) {
		#save_model_information(mixed_model())
		#stop("Arguments are missing. First Argument has to be the path of the input File, 
		#	second the Path of the output File and third the formula for the model", call.=FALSE)
		quit(status=2)
	} else if (!file.exists(args[1])) {
		#stop("Your input File does not Exists", call.=FALSE)
		quit(status=3)
	} else if (!checkFormula(args[3])) {
	    #stop("Not a valid formular", call.=FALSE)
		quit(status=4)
	}
	return (args)
}

#Used for testing
#TODO delete
modelTest <- function(formu) {
	library(lme4)
	data(sleepstudy, package='lme4')
	
	m <- lmer(as.formula(formu), data=sleepstudy)
	#summary(m)
	return (m)
}

#Check if the formula is valid
#@return TRUE if it is valid
#@reutrn FALSE if an error occured, which means that the formula is not valid
checkFormula <- function(formu) {
	tryCatch(
			{
				#as.formula("fewfkkervp675879orv")
				as.formula(formu)
				return(TRUE)
			},
			error=function(cond){
				return (FALSE)
			}
	)
}

#test()
args <- checkArgs()
inputPath <- args[1]
outputPath <- args[2]
formu <- args[3]
#mixed_model("C:/Users/msant/Downloads/Dimmer/src/main/java/dk/sdu/imada/mixed_model/CSV_FILE_NAME0.csv","pvalue ~ status")
#save_model_information(mixed_model("C:/Users/msant/Downloads/Dimmer/src/main/java/dk/sdu/imada/mixed_model/CSV_FILE_NAME0.csv","pvalue ~ status + (1|Person_ID)"),"C:/Users/msant/Downloads/Dimmer/src/main/java/dk/sdu/imada/mixed_model/results0.csv")
#save_model_information(modelTest(inputPath, formu), outputPath)
save_model_information(mixed_model(inputPath, formu), outputPath)


#v <- sleepMod@resp
#v <- v$mu
#print(predict(sleepMod)[1:3])
#print(v[1:3])
#save_model_information(sleepMod)
#m <- mixed_model()

