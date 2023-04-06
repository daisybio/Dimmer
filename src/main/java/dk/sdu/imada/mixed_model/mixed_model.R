#Reads data and performs a mixed Model on them.
#@param inputPath Path to the data file.
#@param formu formula, on which the mixed Model calculates.
#@return m mixed Model
mixed_model <- function (methylData, formu) {
  	library(lme4)	
 	m <- lme4::lmer(as.formula(formu), data=methylData)

	# the following part can be used to calculate p-values with the LMM:
	# source: https://ase.tufts.edu/bugs/guide/assets/mixed_model_guide.html	
	library(car)
	a <- car::Anova(m)
	pvalue <- a$`Pr(>Chisq)`#Wald chi-square
	return(pvalue)

	#return(m)
}

prepareData <- function(inputFile, formu, betaFile, indexFile) {
	betaData <- read.csv(betaFile, header=FALSE)
	indexData <- read.csv(indexFile, header=FALSE)
	methylData <- read.csv(inputFile, header = TRUE)
	
	y <- c()
	pvalue <- c()
	for (i in 1:nrow(betaData)) {
		for(j in indexData) {
			y <- append(y, betaData[i, j])
		}
		y <- data.frame(y)
		colnames(y) <- "beta_values"
		data <- data.frame(methylData, y)
		pvalue <- append(pvalue, mixed_model(data, formu))
	}
	return(pvalue)
}

runModel <- function(inputFile, formu, betaFile, indexFile, outputPath) {
	pvalue <- prepareData(inputFile, formu, betaFile, indexFile)
	save_model_information(pvalue, outputPath)
}

#Saves necessary information of the mixed Model
#@param model mixed Model
#@param outputPath Path of folder, in which the data should be saved.
save_model_information <- function(pvalue, outputPath) {
	#res = model@resp$mu
	#res <- as.list(res)
	#para = model@beta #used
	#res <- append(res, para)
	#stdErr = summary(model)$coef[, 2, drop = FALSE]#used
	#test <- append(para, stdErr)#used

	#print (test)
	#names(frame)[ncol(frame)] <-region	
	#print(frame)
	#print(res)
	#library(Matrix) #used
	#write.table(matrix(test, nrow=1), outputPath, sep=", ", row.names=FALSE, col.names=FALSE)#used
	write.table(pvalue, file=outputPath, row.names=FALSE, col.names=FALSE, sep=",")
}

#Used for testing
#TODO delete
test <- function() {
	#args[1] = "C:/Users/msant/Downloads/Dimmer/src/main/java/dk/sdu/imada/mixed_model/CSV_FILE_NAME.csv"
	#args[2] = "C:/Users/msant/Downloads/Dimmer/src/main/java/dk/sdu/imada/mixed_model/results.csv"
	#args[3] = "Reaction ~ Days + (Days|Subject)"
	
	betaData <- matrix(1:9, nrow=3, ncol=3)
	indexData <- c(1,2,3)
	
	
	print(betaData)
	for (i in 1:nrow(betaData)) {
		res <- c()
		for(j in indexData) {
			res <-append(res, betaData[i, j])
		}
		res <- data.frame(res)
		colnames(res) <- "beta_value"
		data <- data.frame(betaData, res)
		print(data)
	}
	save_model_information(indexData, "/home/scibiome/Documents/test.csv")
}

#Checks if the arguments are complete and correct
#If not quits with the error-code
#2, if there aren,t enough parameters.
#3, if the input file does not exists.
#4, if the formula is not valid.
#@return args the arguments
checkArgs <- function() {
	args = commandArgs(trailingOnly=TRUE)
	if (length(args) < 5) {
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
	} else if (!file.exists(args[4])) {
		quit(status=5)
	} else if (!file.exists(args[5])) {
		quit(status=6)
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
	
	library(car)
	a <- car::Anova(m)
	pvalue <- a$`Pr(>Chisq)`#Wald chi-square
	print(pvalue)
	return(pvalue)
	#return (m)
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
betaFile <- args[4]
indexFile <- args[5]

options(error=function() traceback(2))

runModel(inputPath, formu, betaFile, indexFile, outputPath)

#save_model_information(mixed_model("C:/Users/msant/Downloads/dimmer_testfiles/dimmer_testfiles/extended_regression_mm/mm_tmp_in_0.csv","beta_value ~ status + (1|Person_ID)"),"C:/Users/msant/Downloads/Dimmer/src/main/java/dk/sdu/imada/mixed_model/results0.csv")
#save_model_information(modelTest(inputPath, formu), outputPath)
#save_model_information(mixed_model(inputPath, formu, betaFile, indexFile), outputPath)
#save_model_information(modelTest("Reaction ~ Days + (1|Subject)"), "C:/Users/msant/Downloads/Dimmer/src/main/java/dk/sdu/imada/mixed_model/CSV_FILE_NAME0.csv")


#v <- sleepMod@resp
#v <- v$mu
#print(predict(sleepMod)[1:3])
#print(v[1:3])
#save_model_information(sleepMod)
#m <- mixed_model()

