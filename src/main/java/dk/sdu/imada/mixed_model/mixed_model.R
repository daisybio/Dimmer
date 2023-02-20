mixed_model <- function (inputPath, formu) {
  	library(lme4)
  	methylData <- read.csv(inputPath, header=TRUE)
	
 	#config <- config::get()
 	#m <- lmer(config$formula, data=data)
	#print(formu)
	#print(methylData)
 	m <- lmer(as.formula(formu), data=methylData)
 	#print(typeof(m))
 	#return (predict(m, re.form=NA))
	#summary(m)
	#print("Model finished calculating")
	return(m)
}

save_model_information <- function(model, outputPath) {
	res = model@resp$mu
	#res <- as.list(res)
	para = model@beta
	#res <- append(res, para)
	stdErr = summary(model)$coef[, 2, drop = FALSE]
	test <- append(para, stdErr)

	#print (test)
	
	#if (file.exists(outputPath)) {
	#	frame <- read.csv(outputPath)
	#	frame <- cbind(frame, res)
	#} else {
	#	frame <- data.frame(res)
	#}
	#names(frame)[ncol(frame)] <-region	
	#print(frame)
	#print(res)
	library(Matrix)
	write.table(matrix(test, nrow=1), outputPath, sep=", ", row.names=FALSE, col.names=FALSE)
	#print("Model saved Data")
}

test <- function() {
	args[1] = "C:/Users/msant/Downloads/Dimmer/src/main/java/dk/sdu/imada/mixed_model/CSV_FILE_NAME.csv"
	args[2] = "C:/Users/msant/Downloads/Dimmer/src/main/java/dk/sdu/imada/mixed_model/results.csv"
	args[3] = "Reaction ~ Days + (Days|Subject)"
}

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
		quit(status=3)
	}
	return (args)
}

modelTest <- function(formu) {
	library(lme4)
	data(sleepstudy, package='lme4')
	
	m <- lmer(as.formula(formu), data=sleepstudy)
	#summary(m)
	return (m)
}

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

#print(colnames(sleepstudy))

#untar(tarfile = "C:/Users/msant/Desktop/Uni/Bachelorarbeit/Dimmer-2.1/src/main/java/dk/sdu/imada/mixed_model/GSE86831_RAW.tar", exdir = "C:/Users/msant/Desktop/Uni/Bachelorarbeit/Dimmer-2.1/src/main/java/dk/sdu/imada/mixed_model/data/")
#mydata <- gzfile(description = "C:/Users/msant/Desktop/Uni/Bachelorarbeit/Dimmer-2.1/src/main/java/dk/sdu/imada/mixed_model/data/GPL21145_MethylationEPIC_15073387_v-1-0.csv.gz", open = '',encoding = getOption("encoding"),
#       compression = 6)
#mydata <- readLines(mydata)
#print(mydata)
