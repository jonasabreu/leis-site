#! /bin/bash

echo "Rodando gerador"
sbt run

echo "adicionando arquivos jekyll"
mv jekyll-files/* site/.

echo "carregando deps jekyll e rvm"
cd site
rvm use 2.1.0
bundle update

echo "rodando jekyll"
jekyll build

echo "deploy"

/home/ubuntu/bin/s3-jekyll-deploy/s3-jekyll-deploy leis.vidageek.net

