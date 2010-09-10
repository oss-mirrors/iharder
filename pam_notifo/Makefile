#CC=clang

all: pam_notifo.so

# This is at least the correct way to compile for Mac OS X Snow Leopard with curl
pam_notifo.so: pam_notifo.c 
	${CC} -lcurl -lpam -bundle -flat_namespace -undefined suppress -o pam_notifo.so pam_notifo.c
