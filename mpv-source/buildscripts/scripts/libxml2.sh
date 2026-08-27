#!/bin/bash -e

. ../../include/path.sh

# F-Droid's libxml2 srclib removes fuzz test sources. Remove their automake
# references before autoreconf; the production library does not use them.
if [ ! -d fuzz ]; then
	sed -i 's/ fuzz//g' Makefile.am
	sed -i 's# fuzz/Makefile##g' configure.ac
fi

[ -f configure ] || ./autogen.sh --host=$ndk_triple --without-python

if [ "$1" == "build" ]; then
	true
elif [ "$1" == "clean" ]; then
	make clean
	exit 0
else
	exit 255
fi

$0 clean # separate building not supported, always clean

./configure \
	--host=$ndk_triple \
	--without-python \
	--without-iconv
make -j$cores
make DESTDIR="$prefix_dir" install
