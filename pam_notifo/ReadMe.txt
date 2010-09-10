Project: pam_notifo
Description: A Pluggable Authentication Module (PAM) for sending notifications to Notifo.com.
Author: Robert Harder
Homepage: http://iharder.net/pam_notifo
License: Public Domain

This is a crude attempt at a PAM module (sorry, it's redundant) that sends notifications via Notifo.com when someone logs in.


INSTALLATION

If you have a Mac, you might not need to recompile the module. Try copying pam_notifo.so to /usr/lib/pam first.

If you have another PAM-compliant system, you'll need to compile the pam_notifo.c file. Try running 'make' at the command line, but I have no idea if it will compile on anything else since this is something like version 0.0001.

Edit your PAM configuration files. On Mac OS X these are at /etc/pam.d. For example to be notified when anyone logs in via SSH, modify your /etc/pam.d/sshd file to look something like this:

# sshd: auth account password session
auth       optional       pam_krb5.so
auth       optional       pam_mount.so
auth       sufficient     pam_serialnumber.so serverinstall legacy
auth       required       pam_opendirectory.so
account    required       pam_nologin.so
account    required       pam_sacl.so sacl_service=ssh
account    required       pam_opendirectory.so
password   required       pam_opendirectory.so
session    required       pam_launchd.so
session    optional       pam_mount.so
session    optional       pam_notifo.so notifo_user=johndoe notifo_api_key=062c10a7b43cb9b6634dfdc20b37e070

(That last line should all be on one line).

You'll have to fill in your Notifo username and API key of course.

You can add this line to any of the other files in /etc/pam.d if you want notification when, for instance, someone changes their password (/etc/sshd/passwd) or even opens a Terminal window (/etc/sshd/login.term -- sounds annoying to me).

Enjoy.

-Rob
