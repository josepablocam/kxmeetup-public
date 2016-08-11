.PHONY: clean

all:
	pdflatex main
	bibtex main
	pdflatex main
	pdflatex main
	mv main.pdf presentation.pdf
	make clean
	
	
clean:
	rm -rf main.pdf *.aux *.bbl *.blg *.log *.nav *.out *.toc *.vrb *.snm *.synctex.*