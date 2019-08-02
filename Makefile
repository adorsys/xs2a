WORKDIR=${PWD}


# https://www.gnu.org/software/make/manual/make.html#Phony-Targets
.PHONY : clean usage pages

usage :
	@echo "Use \"make clean\" or \"make pages\""

.PHONY : prepare_pages
prepare_pages:
	mkdir -p pages pages/html pages/html/doc
	rsync -armR --include="*/" --include="*.adoc" --exclude="*" doc/ pages
	rsync -armR --include="*/" --include="*.puml" --exclude="*" doc/ pages
	rsync -armR --include="*/" --include="*.png" --exclude="*" doc/ pages

.PHONY : prepare_diagrams
prepare_diagrams: prepare_pages
	cd pages/doc && plantuml **/*.puml

pages : prepare_pages prepare_diagrams
	cd pages && asciidoctor --failure-level WARN -R doc -D html '**/*.adoc'
	cd pages && rsync -amR --include="*/" --include="*.png" --exclude="*" doc/ html
	cd pages && cd html/doc && rsync -amR --include="*/" --include="*.png" --exclude="*" . ..
	cd pages && cd html && rm -rf doc

clean :
	-rm -r pages
	mvn clean

build: maven_build pages

maven_build:
	mvn install

all: clean build
