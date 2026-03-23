#!/bin/bash

# BCBBS3 Log Viewer Script
# Quick access to different log files

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKEND_LOGS="$PROJECT_ROOT/backend/logs"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

show_help() {
    echo -e "${BLUE}BCBBS3 Log Viewer${NC}"
    echo "Usage: $0 [OPTION] [LINES]"
    echo ""
    echo "Options:"
    echo "  -a, --all       View all logs (app.log)"
    echo "  -e, --error     View error logs (error.log)"
    echo "  -w, --warn      View warning logs (warn.log)"
    echo "  -s, --security  View security logs (security.log)"
    echo "  -b, --business  View business logs (business.log)"
    echo "  -f, --frontend  View frontend logs (frontend.log)"
    echo "  -api            View API request logs (api.log)"
    echo "  -d, --debug     View debug logs (debug.log)"
    echo "  --tail          Follow log in real-time (like tail -f)"
    echo "  --grep          Search for pattern in all logs"
    echo "  -h, --help      Show this help"
    echo ""
    echo "Examples:"
    echo "  $0 -e 50        # Show last 50 error lines"
    echo "  $0 -a --tail    # Follow app.log in real-time"
    echo "  $0 --grep \"ERROR\" # Search ERROR in all logs"
}

LINES=50
TAIL_MODE=false
GREP_PATTERN=""
LOG_FILE=""

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -a|--all)
            LOG_FILE="$BACKEND_LOGS/app.log"
            shift
            ;;
        -e|--error)
            LOG_FILE="$BACKEND_LOGS/error.log"
            shift
            ;;
        -w|--warn)
            LOG_FILE="$BACKEND_LOGS/warn.log"
            shift
            ;;
        -s|--security)
            LOG_FILE="$BACKEND_LOGS/security.log"
            shift
            ;;
        -b|--business)
            LOG_FILE="$BACKEND_LOGS/business.log"
            shift
            ;;
        -f|--frontend)
            LOG_FILE="$BACKEND_LOGS/frontend.log"
            shift
            ;;
        -api)
            LOG_FILE="$BACKEND_LOGS/api.log"
            shift
            ;;
        -d|--debug)
            LOG_FILE="$BACKEND_LOGS/debug.log"
            shift
            ;;
        --tail)
            TAIL_MODE=true
            shift
            ;;
        --grep)
            GREP_PATTERN="$2"
            shift 2
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        [0-9]*)
            LINES=$1
            shift
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# Check if backend logs directory exists
if [ ! -d "$BACKEND_LOGS" ]; then
    echo -e "${RED}Error: Log directory not found: $BACKEND_LOGS${NC}"
    exit 1
fi

# Handle grep mode
if [ -n "$GREP_PATTERN" ]; then
    echo -e "${BLUE}Searching for '${GREP_PATTERN}' in all logs...${NC}"
    grep -r --include="*.log" --color=auto "$GREP_PATTERN" "$BACKEND_LOGS" 2>/dev/null || echo "No matches found"
    exit 0
fi

# Default to app.log if no file specified
if [ -z "$LOG_FILE" ]; then
    LOG_FILE="$BACKEND_LOGS/app.log"
fi

# Check if log file exists
if [ ! -f "$LOG_FILE" ]; then
    echo -e "${YELLOW}Warning: Log file not found: $LOG_FILE${NC}"
    echo "File will be created when first log entry is written."
    exit 0
fi

# Show log file info
echo -e "${GREEN}Viewing: $(basename "$LOG_FILE")${NC}"
echo -e "${BLUE}Path: $LOG_FILE${NC}"
echo -e "${BLUE}Size: $(du -h "$LOG_FILE" | cut -f1)${NC}"
echo "---"

# Display logs
if [ "$TAIL_MODE" = true ]; then
    echo -e "${YELLOW}Following log (Press Ctrl+C to exit)...${NC}"
    tail -f "$LOG_FILE"
else
    tail -n "$LINES" "$LOG_FILE" | while IFS= read -r line; do
        # Colorize log levels
        if echo "$line" | grep -q "ERROR"; then
            echo -e "${RED}${line}${NC}"
        elif echo "$line" | grep -q "WARN"; then
            echo -e "${YELLOW}${line}${NC}"
        elif echo "$line" | grep -q "DEBUG"; then
            echo -e "${BLUE}${line}${NC}"
        else
            echo "$line"
        fi
    done
fi
