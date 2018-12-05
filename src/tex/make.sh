#!/bin/sh
pdflatex -shell-escape -interaction=nonstopmode doc 
biber doc
pdflatex -shell-escape -interaction=nonstopmode doc 