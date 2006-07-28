# Filedrop Makefile
# SVN: https://svn.sourceforge.net/iharder/filedrop
# Command: make
# Output: filedrop.jar


# Programs and flags
CC	= javac
CP	= cp
JAR	= jar
RM	= rm
RMFLAGS	= -rf
CFLAGS	= -d $(BUILD)

# Directories
BUILD	= build
PKG	= net/iharder/dnd

# Final Output
PROG	= filedrop.jar


# Default entrance to makefile.
all: $(PROG) 


# Make jar file
$(PROG) : classes $(BUILD)/manifest
	$(JAR) cfm $@ $(BUILD)/manifest -C $(BUILD) $(PKG)


# Target directory
$(BUILD):
	mkdir $(BUILD)


# Build all classes. Needs target directory.
classes: $(BUILD)
	$(CC) $(CFLAGS) $(PKG)/*.java


# Build manifest file. Needs target directory.
$(BUILD)/manifest: $(BUILD)
	echo Manifest-Version: 1.0 >  $@
	echo Main-Class: net/iharder/dnd/FileDrop >>  $@




# Delete build directory and final output.
clean:
	$(RM) $(RMFLAGS) $(BUILD)
	$(RM) $(RMFLAGS) $(PROG)




