#!/bin/sh

# Strips off header, footer, and line numbers from TSPLIB format data sets
cat $1 | tail -n +8 | cut -d ' ' -f 2-3 | grep -v 'EOF'
