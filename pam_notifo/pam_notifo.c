/**
 * pam_notifo.c
 *
 * The PAM config file should contain the Notifo username and API key like so:
 *
 * session    optional       pam_notifo.so notifo_user=johndoe notifo_api_key=062c10a7b43cb9b6634dfdc20b37e070
 *
 * To compile you'll need something like this:
 *
 * cc -lcurl -lpam -bundle -flat_namespace \
 *    -undefined suppress -o pam_notifo.so pam_notifo.c
 *
 * Honestly I don't know which of the flags are unique to Mac OS X where I
 * developed the plugin. I'd be glad for input to make this more cross-platform
 * friendly. I'm not familiar with automake, et al.
 *
 * Helpful references: 
 *  - http://linux.die.net/man/3/pam
 *  - http://curl.haxx.se/docs/manpage.html
 * 
 * @author Robert Harder, rob _ iharder.net
 * @license Public Domain
 */


#include <security/pam_modules.h>
#include <security/pam_appl.h>
#include <security/openpam.h>
#include <stdio.h>
#include <stdlib.h>
#include <syslog.h>
#include <unistd.h>
#include <curl/curl.h>


#define NOTIFO_MESSAGE_LENGTH 500
#define MAXHOSTNAMELEN 500

#define PAM_NOTIFO_USER_KEY "notifo_user"
#define PAM_NOTIFO_API_KEY  "notifo_api_key"

#define NOTIFO_API_URL "https://api.notifo.com/v1/send_notification"
#define NOTIFO_LABEL_KEY "label"
#define NOTIFO_TITLE_KEY "title"
#define NOTIFO_MESSAGE_KEY "msg"
#define NOTIFO_URL_KEY "url"




/**
 * Receives the response from CURL (and silently ignores it).
 */
int __notifo_response( void *ptr, size_t size, size_t nmemb, void *userdata){
    int numBytes = size*nmemb;
    char *resp = (char *)malloc(numBytes+1);
    resp[numBytes] = 0;
    snprintf( resp, numBytes, "%s", (char *)ptr);
    openlog("pam_notifo", 0, LOG_AUTHPRIV);
    syslog(LOG_WARNING, "Remote Notifo Response: %s", resp);
    return 0;
}


/**
 * Sends a Notifo notification.
 */
int curl_notifo( const char *username, const char *apiKey, char *label, char *title, char *message, char *url ){
    
    char userApi[500];
    int ret;
    CURL *curl;                             // The request object
    CURLcode res;                           // Responses from curl functions
    struct curl_httppost *formpost=NULL;    // Contains POST data
    struct curl_httppost *lastptr=NULL;     // Linked list for POST data
    
    if( username == NULL || apiKey == NULL ){
        return 2;
    } else {
        ret = snprintf( userApi, 500, "%s:%s", username, apiKey );
        if( ret < 0 ){ return 3; }
        if( ret >= 500 ){ return 4; }
    }
    
    curl = curl_easy_init();
    if(curl) {
        curl_easy_setopt(curl, CURLOPT_NOPROGRESS, 1L);             // No progress meter
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, __notifo_response);  // Receive response here
        curl_easy_setopt(curl, CURLOPT_URL, NOTIFO_API_URL);        // The Notifo API URL
        curl_easy_setopt(curl, CURLOPT_HTTPAUTH, CURLAUTH_BASIC);   // Use http basic authentication
        curl_easy_setopt(curl, CURLOPT_USERPWD, userApi);           // Set the username and API key
        
        // Set up the POST data with the Notifo notification information
        if( label ){
            curl_formadd(&formpost, &lastptr, CURLFORM_COPYNAME, NOTIFO_LABEL_KEY, CURLFORM_COPYCONTENTS, label, CURLFORM_END);
        }
        if( title ){
            curl_formadd(&formpost, &lastptr, CURLFORM_COPYNAME, NOTIFO_TITLE_KEY, CURLFORM_COPYCONTENTS, title, CURLFORM_END);
        }
        if( message ){
            curl_formadd(&formpost, &lastptr, CURLFORM_COPYNAME, NOTIFO_MESSAGE_KEY, CURLFORM_COPYCONTENTS, message, CURLFORM_END);
        }
        if( url ){
            curl_formadd(&formpost, &lastptr, CURLFORM_COPYNAME, NOTIFO_URL_KEY, CURLFORM_COPYCONTENTS, url, CURLFORM_END);
        }
        
        // Set the POST data
        curl_easy_setopt(curl, CURLOPT_HTTPPOST, formpost);
        
        // Perform the remote POST
        res = curl_easy_perform(curl);
        
        // Always clean up
        curl_easy_cleanup(curl);
        
        return res;
    } else {
        return -1; // CURL not inited properly
    }
}





/**
 * Called when someone logs in.
 */
int pam_sm_open_session(pam_handle_t *pamh, int flags, int argc, const char **argv){ 
    int ret;
    int pam_err;
    const char *pam_user = NULL;
    char *pam_service = NULL;
    char *pam_tty = NULL;
    char *pam_rhost = NULL;
    const char *notifo_user = NULL;
    const char *notifo_api_key = NULL;
    char hostname[MAXHOSTNAMELEN];
    char notifoMessage[NOTIFO_MESSAGE_LENGTH];
    
    openlog("pam_notifo", 0, LOG_AUTHPRIV);
    
    // This system
    gethostname(hostname, sizeof(hostname));
    
    // Get username who is logging in
    pam_err = pam_get_user(pamh, &pam_user, NULL);
    if (pam_err != PAM_SUCCESS) {
        pam_user = "Unknown User";
        syslog(LOG_WARNING, "Could not determine incoming username", pam_user);
    }
    
    // Get service type
    pam_err = pam_get_item(pamh, PAM_SERVICE, (const void **)&pam_service);
    if (pam_err != PAM_SUCCESS || pam_service == NULL) {
        pam_service = "Unknown Service";
        syslog(LOG_WARNING, "Could not determine incoming service type for user %s", pam_user);
    }
    
    // Get TTY
    // This is an alternative to rhost
    pam_err = pam_get_item(pamh, PAM_TTY, (const void **)&pam_tty);
    if (pam_err != PAM_SUCCESS ){
        pam_tty = "Unknown TTY";
        syslog(LOG_WARNING, "Could not determine incoming TTY for user %s", pam_user);
    }
    
    // Get remote host
    // This is an alternative to tty
    pam_err = pam_get_item(pamh, PAM_RHOST, (const void **)&pam_rhost);
    if (pam_err != PAM_SUCCESS ){
        pam_rhost = "Unknown RHOST";
        syslog(LOG_WARNING, "Could not determine incoming remote host for user %s", pam_user);
    }
    
    
    // Get Notifo user info
    notifo_user = openpam_get_option(pamh, PAM_NOTIFO_USER_KEY);
    if( notifo_user == NULL ) {
        syslog(LOG_WARNING, "Notifo username not provided in '%s' pam config file.", pam_service);
        return PAM_SESSION_ERR;
    }
    
    // Get Notifo API key
    notifo_api_key = openpam_get_option(pamh, PAM_NOTIFO_API_KEY);
    if( notifo_api_key == NULL ) {
        syslog(LOG_WARNING, "Notifo API key not provided in '%s' pam config file.", pam_service);
        return PAM_SESSION_ERR;
    }
    
        
    // Build message
    ret = snprintf( notifoMessage, NOTIFO_MESSAGE_LENGTH, "User: %s, Source: %s, Service: %s", 
                   pam_user, 
                   (pam_tty != NULL ? pam_tty : (pam_rhost != NULL ? pam_rhost : "unknown") ),
                   pam_service );
    
    // Send Notifo notification
    curl_notifo(notifo_user, notifo_api_key, hostname, "User Logged In", notifoMessage, NULL);
    
    // Log
    syslog(LOG_NOTICE, "User Logged In: %s\n", notifoMessage);
    
    return PAM_SUCCESS; 

}

int pam_sm_close_session(pam_handle_t *pamh, int flags, int argc, const char **argv){
    return PAM_SUCCESS;
}

