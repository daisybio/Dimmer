FROM ubuntu:20.04
LABEL description="Image for DiMmer"
LABEL maintainer="Alexander Dietrich"

# needed so that R installation does not get stuck
ENV DEBIAN_FRONTEND noninteractive

# install system dependencies
RUN apt-get update -y && apt-get --no-install-recommends --fix-broken install -y git \
    wget \
    vim \
    software-properties-common \
    dirmngr \
    gdebi \
    curl \
    libicu-dev \ 
    cmake

# install JDK8 including JFX
WORKDIR /opt
ENV JDK_VERSION 11.0.16.1
RUN wget https://cdn.azul.com/zulu/bin/zulu11.58.23-ca-jdk${JDK_VERSION}-linux_x64.tar.gz && tar -xzvf zulu11.58.23-ca-jdk${JDK_VERSION}-linux_x64.tar.gz && chmod 777 zulu11.58.23-ca-jdk${JDK_VERSION}-linux_x64/*
ENV PATH=$PATH:/opt/zulu11.58.23-ca-jdk${JDK_VERSION}-linux_x64/bin

# install R 4.2.1 https://docs.posit.co/resources/install-r/
RUN curl -O https://cdn.rstudio.com/r/ubuntu-2004/pkgs/r-4.2.1_1_amd64.deb
RUN gdebi r-4.2.1_1_amd64.deb -n
RUN ln -s /opt/R/4.2.1/bin/R /usr/bin/R
RUN ln -s /opt/R/4.2.1/bin/Rscript /usr/bin/Rscript

# setup renv to handle R packages
RUN R -e "install.packages(c('remotes','renv'), repos = c(CRAN = 'https://cloud.r-project.org'))"

# install all packages
COPY renv.lock renv.lock
RUN R -e "renv::restore()"

# download DiMmer jar
WORKDIR /bin
RUN wget https://github.com/baumbachlab/Dimmer/releases/download/2.3/dimmer.jar 

CMD [ "java", "-jar", "/bin/dimmer.jar"]
