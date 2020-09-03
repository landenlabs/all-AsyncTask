#!/bin/csh -f

set dir=backup
set file=`date "+netDiag_%Y-%m-%d-%H-%M.zip"`

find . -name \.DS_Store -print -exec rm {} +
zip -r $dir/$file . -x .git\* -x \*.idea\* -x captures\* -x .gradle\* -x \*build\* -x \*release\* -x \*apk  -x \*backup\*
