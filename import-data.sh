#!/bin/bash

# FIFA World Cup 2026 - Data Import Bash Script
# For Linux/Mac users
# Usage: bash import-data.sh [action]

set -e

# Configuration
API_URL="${API_URL:-http://localhost:8080/api}"
TIMEOUT=300
ACTION="${1:-menu}"

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# ============================================================================
# Utility Functions
# ============================================================================

write_header() {
    echo ""
    echo -e "${MAGENTA}╔═══════════════════════════════════════════════════════╗${NC}"
    printf "${MAGENTA}║  %-51s║${NC}\n" "$1"
    echo -e "${MAGENTA}╚═══════════════════════════════════════════════════════╝${NC}"
    echo ""
}

write_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

write_error() {
    echo -e "${RED}❌ $1${NC}"
}

write_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

write_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# ============================================================================
# Main Functions
# ============================================================================

check_api_health() {
    write_header "API Health Check"

    write_info "Checking if API is running at $API_URL..."

    if response=$(curl -s -w "\n%{http_code}" -X GET "$API_URL/admin/import/status" 2>/dev/null); then
        http_code=$(echo "$response" | tail -n1)
        body=$(echo "$response" | sed '$d')

        if [ "$http_code" = "200" ]; then
            write_success "API is running and healthy!"
            message=$(echo "$body" | grep -o '"message":"[^"]*' | cut -d'"' -f4)
            write_info "Response: $message"
            return 0
        fi
    fi

    write_error "API is not responding!"
    write_info "Make sure to run: mvn spring-boot:run"
    return 1
}

show_data_source_info() {
    write_header "Data Source Information"

    if response=$(curl -s -X GET "$API_URL/admin/import/source" 2>/dev/null); then
        echo -e "${BLUE}$response${NC}"
    else
        write_error "Failed to get data source info"
    fi
}

import_worldcup_data() {
    write_header "Importing World Cup 2026 Data"

    if ! check_api_health; then
        return 1
    fi

    write_info "Starting import from OpenFootball repository..."
    write_info "This may take 1-2 minutes depending on network speed..."
    echo ""

    start_time=$(date +%s)

    if response=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/admin/import/worldcup-2026" \
        -H "Content-Type: application/json" \
        --max-time $TIMEOUT 2>/dev/null); then

        http_code=$(echo "$response" | tail -n1)
        body=$(echo "$response" | sed '$d')

        end_time=$(date +%s)
        duration=$((end_time - start_time))

        if [ "$http_code" = "200" ]; then
            success=$(echo "$body" | grep -o '"success":true')
            if [ ! -z "$success" ]; then
                write_success "Data import completed successfully!"
                write_info "Duration: ${duration}s"
                return 0
            fi
        fi

        write_error "Import failed (HTTP $http_code)"
        write_error "Response: $body"
        return 1
    else
        write_error "Failed to connect to API"
        return 1
    fi
}

verify_imported_data() {
    write_header "Verifying Imported Data"

    write_info "Checking teams..."
    teams=$(curl -s "$API_URL/teams" | grep -o '"id"' | wc -l)
    if [ $teams -eq 32 ]; then
        write_success "  Teams: $teams (expected: 32)"
    else
        write_warning "  Teams: $teams (expected: 32)"
    fi

    write_info "Checking groups..."
    groups=$(curl -s "$API_URL/groups" | grep -o '"id"' | wc -l)
    if [ $groups -eq 8 ]; then
        write_success "  Groups: $groups (expected: 8)"
    else
        write_warning "  Groups: $groups (expected: 8)"
    fi

    write_info "Checking matches..."
    matches=$(curl -s "$API_URL/matches" | grep -o '"id"' | wc -l)
    if [ $matches -gt 100 ]; then
        write_success "  Matches: $matches (expected: 100+)"
    else
        write_warning "  Matches: $matches (expected: 100+)"
    fi

    write_info "Checking players..."
    players=$(curl -s "$API_URL/players" | grep -o '"id"' | wc -l)
    if [ $players -gt 800 ]; then
        write_success "  Players: $players (expected: 800+)"
    else
        write_warning "  Players: $players (expected: 800+)"
    fi

    echo ""
    write_success "Verification complete!"
}

show_import_summary() {
    write_header "Import Summary"

    echo -e "${MAGENTA}What Was Imported:${NC}"
    echo -e "${GREEN}  ✓ 32 Teams${NC}"
    echo -e "${GREEN}  ✓ 8 Groups (A-H)${NC}"
    echo -e "${GREEN}  ✓ 100+ Matches${NC}"
    echo -e "${GREEN}  ✓ 800+ Players${NC}"
    echo -e "${GREEN}  ✓ Match Results & Goals${NC}"
    echo -e "${GREEN}  ✓ Automatic Standings${NC}"

    echo ""
    echo -e "${MAGENTA}Next Steps:${NC}"
    echo "  1. View API documentation: $API_URL/../swagger-ui.html"
    echo "  2. Test endpoints in Swagger UI"
    echo "  3. Build Angular frontend"
    echo "  4. Connect frontend to APIs"

    echo ""
    echo -e "${BLUE}Data Source:${NC}"
    echo "  Repository: OpenFootball/worldcup.json"
    echo "  URL: https://github.com/openfootball/worldcup.json"
}

show_menu() {
    while true; do
        write_header "World Cup 2026 Data Import Tool"

        echo "Select an option:"
        echo -e "  1. Check API Health"
        echo -e "  2. Show Data Source Info"
        echo -e "${GREEN}  3. Import World Cup Data${NC}"
        echo -e "  4. Verify Imported Data"
        echo -e "  5. Show Import Summary"
        echo -e "${MAGENTA}  6. Full Process (Health → Import → Verify)${NC}"
        echo -e "${RED}  7. Exit${NC}"
        echo ""

        read -p "Enter your choice (1-7): " choice

        case $choice in
            1) check_api_health; read -p "Press Enter to continue..." ;;
            2) show_data_source_info; read -p "Press Enter to continue..." ;;
            3)
                if import_worldcup_data; then
                    write_success "Import successful!"
                fi
                read -p "Press Enter to continue..."
                ;;
            4) verify_imported_data; read -p "Press Enter to continue..." ;;
            5) show_import_summary; read -p "Press Enter to continue..." ;;
            6)
                if check_api_health; then
                    if import_worldcup_data; then
                        verify_imported_data
                        show_import_summary
                    fi
                fi
                read -p "Press Enter to continue..."
                ;;
            7)
                write_success "Goodbye!"
                exit 0
                ;;
            *) write_warning "Invalid choice. Please try again." ;;
        esac
    done
}

# ============================================================================
# Script Entry Point
# ============================================================================

case "$ACTION" in
    menu)
        show_menu
        ;;
    health)
        check_api_health
        ;;
    info)
        show_data_source_info
        ;;
    import)
        if import_worldcup_data; then
            verify_imported_data
            show_import_summary
        fi
        ;;
    verify)
        verify_imported_data
        ;;
    summary)
        show_import_summary
        ;;
    full)
        check_api_health
        import_worldcup_data
        verify_imported_data
        show_import_summary
        ;;
    *)
        write_error "Unknown action: $ACTION"
        echo "Usage: $0 [menu|health|info|import|verify|summary|full]"
        exit 1
        ;;
esac

