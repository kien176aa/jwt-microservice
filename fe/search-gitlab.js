
const sortDefinitions = [
    { field: 'name', label: 'Name' },
    { field: 'price', label: 'Price' },
    { field: 'quantity', label: 'Quantity' },
];
let sortField = null;      // trường đang sắp xếp
let sortDirection = 'asc'; // 'asc' hoặc 'desc'
function generateSortItems() {
    let html = '';
    sortDefinitions.forEach(item => {
        html += `
                <li>
                    <a class="dropdown-item sort-item" data-field="${item.field}">
                    ${item.label}
                    </a>
                </li>
                `;
    });
    $('#sort-dropdown-menu').html(html);

    // Bắt sự kiện click chọn sort
    $('.sort-item').on('click', function() {
        sortField = $(this).data('field');
        // Cập nhật giao diện nút dropdown
        const selectedLabel = sortDefinitions.find(s => s.field === sortField)?.label || 'Sort by';
        $('#sort-label').text(selectedLabel);

        // Mặc định khi chọn trường mới => sortDirection về 'asc'
        // sortDirection = 'asc';
        // $('#sort-direction-btn i').removeClass('fa-arrow-down-wide-short').addClass('fa-arrow-up-wide-short');

        // Tùy bạn muốn auto trigger search hay để người dùng nhấn nút search
        console.log('Sort by:', sortField, '| Direction:', sortDirection);
    });
    $('#sort-direction-btn').on('click', function() {
        if (sortDirection === 'asc') {
            sortDirection = 'desc';
            $(this).find('i')
                .removeClass('fa-arrow-up-wide-short')
                .addClass('fa-arrow-down-wide-short');
        } else {
            sortDirection = 'asc';
            $(this).find('i')
                .removeClass('fa-arrow-down-wide-short')
                .addClass('fa-arrow-up-wide-short');
        }
        console.log("Sort direction:", sortDirection);
    });
}

function convertFiltersToRequestAdvanced(filters, sortBy, sortDir) {
    const request = {
        name: null,
        nameOp: null,
        price: null,
        priceOp: null,
        quantity: null,
        quantityOp: null,
        status: null,
        sortBy: sortBy || null,
        orderBy: sortDir || null
    };

    // Validate and process filters
    if (Array.isArray(filters)) {
        filters.forEach(filter => {
            if (!filter.field || !filter.operator || filter.value === undefined) {
                console.warn('Invalid filter:', filter);
                return;
            }

            const field = filter.field.toLowerCase().trim();
            const operator = filter.operator.trim();
            const value = filter.value;

            switch(field) {
                case 'name':
                    if (typeof value === 'string' && value.trim() !== '') {
                        request.name = value.trim();
                        request.nameOp = operator;
                    }
                    break;

                case 'price':
                    const priceValue = parseFloat(value);
                    if (!isNaN(priceValue)) {
                        request.price = priceValue;
                        request.priceOp = operator;
                    }
                    break;

                case 'quantity':
                    const quantityValue = parseInt(value);
                    if (!isNaN(quantityValue)) {
                        request.quantity = quantityValue;
                        request.quantityOp = operator;
                    }
                    break;

                case 'status':
                    if (value !== null && value !== undefined) {
                        request.status = value === 'true' || value === true || value === '1' || value === 1;
                    }
                    break;

                default:
                    console.warn('Unknown field:', field);
            }
        });
    }

    // Validate sort parameters
    if (sortBy && ['name', 'price', 'quantity'].includes(sortBy.toLowerCase())) {
        request.sortBy = sortBy.toLowerCase();
    }

    if (sortDir && ['asc', 'desc'].includes(sortDir.toLowerCase())) {
        request.orderBy = sortDir.toLowerCase();
    }

    return request;
}

$(document).ready(function() {
    generateSortItems();
    // Sample filter definitions that sẽ được lấy từ BE
    const filterDefinitions = [
        {
            field: 'name',
            type: 'text',
            operator: ['=', '!=', 'like'],
            data: null,
            icon: 'fa-signature'
        },
        {
            field: 'price',
            type: 'number',
            operator: ['=', '>', '<', '<>'],
            data: null,
            icon: 'fa-dollar-sign' // đổi từ 'fa-tag' → 'fa-dollar-sign'
        },
        {
            field: 'quantity',
            type: 'number',
            operator: ['=', '>', '<', '<>'],
            data: null,
            icon: 'fa-boxes' // đổi từ 'fa-tag' → 'fa-boxes' (hoặc 'fa-cubes')
        },
        // {
        //     field: 'priority',
        //     type: 'select',
        //     operator: ['=', '!='],
        //     data: [
        //         {value: 'high', label: 'High'},
        //         {value: 'medium', label: 'Medium'},
        //         {value: 'low', label: 'Low'}
        //     ],
        //     icon: 'fa-flag'
        // },
        // {
        //     field: 'updateDate',
        //     type: 'date',
        //     operator: ['='],
        //     data: null,
        //     icon: 'fa-circle-info'
        // },
        {
            field: 'keyword',
            type: 'text',
            operator: ['=', 'contains'],
            data: null,
            icon: 'fa-search',
            isDefault: true
        }
    ];

    // Active filters
    let activeFilters = [];

    $('#btnSearch').on('click', function(){
        // $('#searchRequest').text(JSON.stringify(activeFilters) + " sort: " + sortField + " " + sortDirection);
        var req = convertFiltersToRequestAdvanced(activeFilters, sortField, sortDirection);
        loadProducts(req);
    });

    // Current filter being entered
    let currentFilter = null;
    let currentOperator = null;

    // Generate filter items
    function generateFilterItems() {
        let filtersHtml = '';

        filterDefinitions.forEach(filter => {
            filtersHtml += `
                        <div class="filter-item" data-filter="${filter.field}">
                            <div class="filter-icon">
                                <i class="fa-solid ${filter.icon || 'fa-filter'}"></i>
                            </div>
                            <div class="filter-content">
                                <div>${capitalizeFirstLetter(filter.field)}</div>
                                <div class="filter-label">Filter by ${filter.field}</div>
                            </div>
                        </div>
                    `;
        });

        $('#search-filters').html(filtersHtml);

        // Handle filter selection
        $('.filter-item').on('click', function() {
            const filterType = $(this).data('filter');
            currentFilter = filterType;

            // Hide filter dropdown
            $('#search-filters').removeClass('show');

            // Find filter definition
            const filterDef = filterDefinitions.find(f => f.field === filterType);

            // Show options for the selected filter
            if (filterDef) {
                showFilterOptions(filterDef);
            }
        });
    }

    // Helper function to capitalize first letter
    function capitalizeFirstLetter(string) {
        return string.charAt(0).toUpperCase() + string.slice(1);
    }

    // Toggle search filters dropdown when clicking on the search input
    $('#gitlab-search').on('focus', function() {
        if (!currentFilter) {
            $('#search-filters').addClass('show');
            $('#filter-options').removeClass('show');
        }
    });

    // Hide all dropdowns when clicking outside
    $(document).on('click', function(event) {
        if (!$(event.target).closest('.search-container').length) {
            $('#search-filters').removeClass('show');
            $('#filter-options').removeClass('show');
        }
    });

    // Show filter options based on filter type
    function showFilterOptions(filterDef) {
        let optionsHtml = '';

        // Create operator selection first
        if (filterDef.operator && filterDef.operator.length > 0) {
            optionsHtml += `<div class="custom-input-form">`;

            // Add operator dropdown
            if (filterDef.operator.length > 1) {
                optionsHtml += `
                            <select id="operator-select" class="operator-select">
                                ${filterDef.operator.map(op => `<option value="${op}">${getOperatorLabel(op)}</option>`).join('')}
                            </select>
                        `;
            } else {
                // If only one operator, set it as current and show hidden input
                currentOperator = filterDef.operator[0];
                optionsHtml += `
                            <div class="mb-2">${getOperatorLabel(currentOperator)}</div>
                            <input type="hidden" id="operator-select" value="${currentOperator}">
                        `;
            }

            // Based on type, show appropriate input
            // if (filterDef.type === 'text') {
            //     // For text input or when data is null
            //     optionsHtml += `
            //         <input type="text" id="filter-value-input" placeholder="Enter ${filterDef.field}...">
            //     `;
            // } else if (filterDef.type === 'select' && filterDef.data && filterDef.data.length > 0) {
            //     // For select with options
            //     optionsHtml += `
            //         <select id="filter-value-select">
            //             ${filterDef.data.map(item => `<option value="${item.value}">${item.label}</option>`).join('')}
            //         </select>
            //     `;
            // } else if (filterDef.type === 'date') {
            //     // For date
            //     optionsHtml += `
            //         <input type="date" id="filter-value-input" placeholder="Enter ${filterDef.field}...">
            //     `;
            // }

            switch (filterDef.type) {
                case 'text':
                case 'number':
                case 'color':
                case 'date':
                case 'datetime-local':
                case 'time':
                    optionsHtml += `<input type="${filterDef.type}" id="filter-value-input" placeholder="Enter ${filterDef.field}...">`;
                    break;

                case 'select':
                    if (filterDef.data && filterDef.data.length > 0) {
                        optionsHtml += `
                                    <select id="filter-value-select">
                                        ${filterDef.data.map(item => `<option value="${item.value}">${item.label}</option>`).join('')}
                                    </select>
                                `;
                    }
                    break;

                default:
                    optionsHtml += `<input type="text" id="filter-value-input" placeholder="Enter ${filterDef.field}...">`;
                    break;
            }

            // Add apply button
            optionsHtml += `
                        <button id="apply-filter" class="btn btn-primary">Apply Filter</button>
                    </div>`;
        }

        $('#filter-options').html(optionsHtml);
        $('#filter-options').addClass('show');

        // Set initial operator
        if (filterDef.operator && filterDef.operator.length > 0) {
            currentOperator = $('#operator-select').val();
        }

        // Handle operator change
        $('#operator-select').on('change', function() {
            currentOperator = $(this).val();
        });

        // Handle apply filter button
        $('#apply-filter').on('click', function() {
            let value, label;

            if (filterDef.type === 'text' || filterDef.data === null) {
                value = $('#filter-value-input').val();
                label = value;
            } else if (filterDef.type === 'select') {
                value = $('#filter-value-select').val();
                label = $('#filter-value-select option:selected').text();
            }

            if (value) {
                addFilter(filterDef.field, value, label, currentOperator);
                $('#filter-options').removeClass('show');
                currentFilter = null;
                currentOperator = null;
            }
        });

        // Focus on first input
        if (filterDef.type === 'text' || filterDef.data === null) {
            $('#filter-value-input').focus();
        }
    }

    // Get label for operator
    function getOperatorLabel(operator) {
        switch(operator) {
            case '=': return 'Equals';
            case '!=': return 'Not Equals';
            case 'contains': return 'Contains';
            case '>': return 'Greater Than';
            case '<': return 'Less Than';
            case '>=': return 'Greater Than or Equal To';
            case '<=': return 'Less Than or Equal To';
            default: return operator;
        }
    }

    // Add filter to active filters
    function addFilter(field, value, label, operator) {
        // Check if filter already exists
        const existingIndex = activeFilters.findIndex(f => f.field === field);
        if (existingIndex >= 0) {
            // Replace existing filter
            activeFilters[existingIndex] = {
                field: field,
                operator: operator,
                value: value,
                label: label
            };
        } else {
            // Add new filter
            activeFilters.push({
                field: field,
                operator: operator,
                value: value,
                label: label
            });
        }

        // Update UI
        updateActiveFiltersUI();

        // Show clear button
        $('#clear-filters').show();
    }

    // Update active filters UI
    function updateActiveFiltersUI() {
        let filtersHtml = '';

        activeFilters.forEach((filter, index) => {
            const operatorSymbol = getOperatorSymbol(filter.operator);
            filtersHtml += `
                        <div class="active-filter" data-index="${index}">
                            <span class="active-filter-label" title="${filter.field} ${operatorSymbol} ${filter.label}">${filter.field} ${operatorSymbol} ${filter.label}</span>
                            <i class="fa-solid fa-times ms-1 filter-chip-remove" data-index="${index}"></i>
                        </div>
                    `;
        });

        $('#active-filters').html(filtersHtml);

        // Handle remove filter
        $('.filter-chip-remove').off('click').on('click', function(e) {
            e.stopPropagation();
            const index = $(this).data('index');
            removeFilter(index);
        });

        // Show reset icon if there are filters
        if (activeFilters.length > 0) {
            $('#reset-search').addClass('visible');
        } else {
            $('#reset-search').removeClass('visible');
            $('#clear-filters').hide();
        }

        // Handle click on active filter
        $('.active-filter').off('click').on('click', function() {
            const index = $(this).data('index');
            const filter = activeFilters[index];
            currentFilter = filter.field;

            // Find filter definition
            const filterDef = filterDefinitions.find(f => f.field === filter.field);

            if (filterDef) {
                showFilterOptions(filterDef);
            }
        });
    }

    // Get symbol for operator display
    function getOperatorSymbol(operator) {
        switch(operator) {
            case '=': return '=';
            case '!=': return '≠';
            case 'contains': return '⊇';
            case '>': return '>';
            case '<': return '<';
            case '>=': return '≥';
            case '<=': return '≤';
            default: return operator;
        }
    }

    // Remove filter
    function removeFilter(index) {
        activeFilters.splice(index, 1);
        updateActiveFiltersUI();
    }

    // Reset search
    $('#reset-search').on('click', function() {
        activeFilters = [];
        $('#gitlab-search').val('');
        updateActiveFiltersUI();
    });

    // Clear all filters
    $('#clear-filters').on('click', function() {
        activeFilters = [];
        $('#gitlab-search').val('');
        updateActiveFiltersUI();
    });

    // Custom search functionality
    $('#gitlab-search').on('keyup', function(e) {
        const searchValue = $(this).val().toLowerCase();

        // If Enter is pressed
        if (e.key === 'Enter') {
            if (currentFilter) {
                // Apply current filter if any
                const filterDef = filterDefinitions.find(f => f.field === currentFilter);
                if (filterDef && filterDef.type === 'text') {
                    addFilter(currentFilter, searchValue, searchValue, currentOperator || '=');
                    $('#filter-options').removeClass('show');
                    currentFilter = null;
                    currentOperator = null;
                    $(this).val('');
                }
            } else {
                console.log('Perform search with filters:', activeFilters, 'and text:', searchValue);
                let defaultFilter = filterDefinitions.find(f => f.isDefault);
                if(defaultFilter){
                    addFilter(defaultFilter.field, searchValue, searchValue, '=');
                    $(this).val('');
                }
            }
        }
    });

    // Initialize filters
    generateFilterItems();
});
