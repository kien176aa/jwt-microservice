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
        keyword: null,
        name: null,
        nameOp: null,
        minPrice: null,
        maxPrice: null,
        minQuantity: null,
        maxQuantity: null,
        quantity: null,
        quantityOp: null,
        status: null,
        sortBy: sortBy || null,
        orderBy: sortDir || null
    };

    // Validate and process filters
    if (Array.isArray(filters)) {
        filters.forEach(filter => {
            if (!filter.field || filter.value === undefined) {
                console.warn('Invalid filter:', filter);
                return;
            }

            const field = filter.field.toLowerCase().trim();
            const operator = filter.operator?.trim();
            const value = filter.value;

            switch(field) {
                case 'keyword':
                    if (typeof value === 'string' && value.trim() !== '') {
                        request.keyword = value.trim();
                    }
                    break;

                case 'name':
                    if (typeof value === 'string' && value.trim() !== '') {
                        request.name = value.trim();
                        request.nameOp = operator;
                    }
                    break;

                case 'price':
                    if (operator === 'range' && typeof value === 'object' && value !== null) {
                        // Xử lý price range
                        const minPrice = parseFloat(value.min);
                        const maxPrice = parseFloat(value.max);

                        if (!isNaN(minPrice) && minPrice >= 0) {
                            request.minPrice = minPrice;
                        }
                        if (!isNaN(maxPrice) && maxPrice >= 0) {
                            request.maxPrice = maxPrice;
                        }
                    } else {
                        // Xử lý price đơn lẻ (nếu có)
                        const priceValue = parseFloat(value);
                        if (!isNaN(priceValue) && priceValue >= 0) {
                            request.minPrice = priceValue;
                            request.maxPrice = priceValue;
                        }
                    }
                    break;

                case 'quantity':
                    if (operator === 'range' && typeof value === 'object' && value !== null) {
                        // Xử lý quantity range
                        const minQuantity = parseInt(value.min);
                        const maxQuantity = parseInt(value.max);

                        if (!isNaN(minQuantity) && minQuantity >= 0) {
                            request.minQuantity = minQuantity;
                        }
                        if (!isNaN(maxQuantity) && maxQuantity >= 0) {
                            request.maxQuantity = maxQuantity;
                        }
                    } else {
                        // Xử lý quantity với các operator khác
                        const quantityValue = parseInt(value);
                        if (!isNaN(quantityValue)) {
                            request.quantity = quantityValue;
                            request.quantityOp = operator;
                        }
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

    // Sample filter definitions
    const filterDefinitions = [
        {
            field: 'keyword',
            type: 'text',
            operator: ['='],
            data: null,
            icon: 'fa-search',
            isDefault: true
        },
        {
            field: 'price',
            type: 'number',
            operator: ['range'],
            data: null,
            icon: 'fa-dollar-sign',
            rangeConfig: {
                minLabel: 'Min Price',
                maxLabel: 'Max Price',
                minPlaceholder: '0',
                maxPlaceholder: '∞',
                step: '0.01'
            }
        },
        {
            field: 'quantity',
            type: 'number',
            operator: ['range'],
            data: null,
            icon: 'fa-boxes',
            rangeConfig: {
                minLabel: 'Min Quantity',
                maxLabel: 'Max Quantity',
                minPlaceholder: '0',
                maxPlaceholder: '∞',
                step: '1'
            }
        }
    ];

    // Active filters
    let activeFilters = [];

    $('#btnSearch').on('click', function(){
        searchProductReq = convertFiltersToRequestAdvanced(activeFilters, sortField, sortDirection);
        currentPageProducts = 1;
        loadProducts(searchProductReq);
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

        optionsHtml += `<div class="custom-input-form">`;

        // Xử lý operator dropdown
        if (filterDef.operator && filterDef.operator.length > 0) {
            // Add operator dropdown
            if (filterDef.operator.length > 1) {
                optionsHtml += `
                            <div class="mb-2">
                                <label class="form-label">Operator</label>
                                <select id="operator-select" class="form-control operator-select">
                                    ${filterDef.operator.map(op => `<option value="${op}">${getOperatorLabel(op)}</option>`).join('')}
                                </select>
                            </div>
                        `;
            } else {
                // If only one operator, set it as current
                currentOperator = filterDef.operator[0];
                optionsHtml += `
                            <div class="mb-2">
                                <label class="form-label">${getOperatorLabel(currentOperator)}</label>
                                <input type="hidden" id="operator-select" value="${currentOperator}">
                            </div>
                        `;
            }

            // Add dynamic input based on selected operator
            optionsHtml += `<div id="dynamic-input-container"></div>`;
        }

        // Add apply button
        optionsHtml += `
                    <button id="apply-filter" class="btn btn-primary">Apply Filter</button>
                </div>`;

        $('#filter-options').html(optionsHtml);
        $('#filter-options').addClass('show');

        // Set initial operator
        if (filterDef.operator && filterDef.operator.length > 0) {
            currentOperator = $('#operator-select').val();
            updateDynamicInput(filterDef, currentOperator);
        }

        // Handle operator change
        $('#operator-select').on('change', function() {
            currentOperator = $(this).val();
            updateDynamicInput(filterDef, currentOperator);
        });

        // Handle apply filter button
        $('#apply-filter').on('click', function() {
            applyCurrentFilter(filterDef);
        });

        // Focus on first input after a short delay
        setTimeout(() => {
            if (currentOperator === 'range') {
                $(`#min-${filterDef.field}`).focus();
            } else {
                $('#filter-value-input').focus();
            }
        }, 100);
    }

    // Update dynamic input based on operator
    function updateDynamicInput(filterDef, operator) {
        let inputHtml = '';

        if (operator === 'range') {
            // Render range input
            const config = filterDef.rangeConfig || {
                minLabel: `Min ${capitalizeFirstLetter(filterDef.field)}`,
                maxLabel: `Max ${capitalizeFirstLetter(filterDef.field)}`,
                minPlaceholder: '0',
                maxPlaceholder: '∞',
                step: filterDef.type === 'number' ? '1' : '0.01'
            };

            inputHtml = `
                        <div class="mb-2">
                            <label class="form-label">${capitalizeFirstLetter(filterDef.field)} Range</label>
                        </div>
                        <div class="range-container">
                            <div class="range-input-group">
                                <label for="min-${filterDef.field}">${config.minLabel}</label>
                                <input type="number" id="min-${filterDef.field}" class="form-control" 
                                       placeholder="${config.minPlaceholder}" min="0" step="${config.step}">
                            </div>
                            <div class="range-separator">-</div>
                            <div class="range-input-group">
                                <label for="max-${filterDef.field}">${config.maxLabel}</label>
                                <input type="number" id="max-${filterDef.field}" class="form-control" 
                                       placeholder="${config.maxPlaceholder}" min="0" step="${config.step}">
                            </div>
                        </div>
                    `;
        } else {
            // Render single input based on type
            switch (filterDef.type) {
                case 'text':
                case 'number':
                case 'color':
                case 'date':
                case 'datetime-local':
                case 'time':
                    inputHtml = `
                                <div class="mb-2">
                                    <label class="form-label">Value</label>
                                    <input type="${filterDef.type}" id="filter-value-input" class="form-control" 
                                           placeholder="Enter ${filterDef.field}...">
                                </div>
                            `;
                    break;

                case 'select':
                    if (filterDef.data && filterDef.data.length > 0) {
                        inputHtml = `
                                    <div class="mb-2">
                                        <label class="form-label">Value</label>
                                        <select id="filter-value-select" class="form-control">
                                            ${filterDef.data.map(item => `<option value="${item.value}">${item.label}</option>`).join('')}
                                        </select>
                                    </div>
                                `;
                    }
                    break;

                default:
                    inputHtml = `
                                <div class="mb-2">
                                    <label class="form-label">Value</label>
                                    <input type="text" id="filter-value-input" class="form-control" 
                                           placeholder="Enter ${filterDef.field}...">
                                </div>
                            `;
                    break;
            }
        }

        $('#dynamic-input-container').html(inputHtml);
    }

    // Apply current filter
    function applyCurrentFilter(filterDef) {
        let value, label;

        if (currentOperator === 'range') {
            // Xử lý range
            const minValue = parseFloat($(`#min-${filterDef.field}`).val()) || null;
            const maxValue = parseFloat($(`#max-${filterDef.field}`).val()) || null;

            if (minValue !== null || maxValue !== null) {
                value = {
                    min: minValue,
                    max: maxValue
                };

                // Tạo label hiển thị
                if (minValue !== null && maxValue !== null) {
                    label = `[${minValue}, ${maxValue}]`;
                } else if (minValue !== null) {
                    label = `≥ ${minValue}`;
                } else if (maxValue !== null) {
                    label = `≤ ${maxValue}`;
                }

                addFilter(filterDef.field, value, label, 'range');
            }
        } else {
            // Xử lý các operator khác
            if (filterDef.type === 'text' || filterDef.type === 'number' || filterDef.data === null) {
                value = $('#filter-value-input').val();
                label = value;
            } else if (filterDef.type === 'select') {
                value = $('#filter-value-select').val();
                label = $('#filter-value-select option:selected').text();
            }

            if (value) {
                addFilter(filterDef.field, value, label, currentOperator);
            }
        }

        if (value) {
            $('#filter-options').removeClass('show');
            currentFilter = null;
            currentOperator = null;
        }
    }

    // Get label for operator
    function getOperatorLabel(operator) {
        switch(operator) {
            case '=': return 'Equals';
            case '!=': return 'Not Equals';
            case 'contains': return 'Contains';
            case 'like': return 'Like';
            case '>': return 'Greater Than';
            case '<': return 'Less Than';
            case '>=': return 'Greater Than or Equal To';
            case '<=': return 'Less Than or Equal To';
            case '<>': return 'Not Equal';
            case 'range': return 'Range';
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
            let displayLabel;

            if (filter.operator === 'range') {
                displayLabel = `${capitalizeFirstLetter(filter.field)}: ${filter.label}`;
            } else {
                displayLabel = `${capitalizeFirstLetter(filter.field)} ${operatorSymbol} ${filter.label}`;
            }

            filtersHtml += `
                        <div class="active-filter" data-index="${index}">
                            <span class="active-filter-label" title="${displayLabel}">${displayLabel}</span>
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

        // Handle click on active filter để edit
        $('.active-filter').off('click').on('click', function() {
            const index = $(this).data('index');
            const filter = activeFilters[index];
            currentFilter = filter.field;

            // Find filter definition
            const filterDef = filterDefinitions.find(f => f.field === filter.field);

            if (filterDef) {
                showFilterOptions(filterDef);

                // Pre-populate values for editing
                setTimeout(() => {
                    if (filter.operator === 'range' && typeof filter.value === 'object') {
                        if (filter.value.min !== null) {
                            $(`#min-${filter.field}`).val(filter.value.min);
                        }
                        if (filter.value.max !== null) {
                            $(`#max-${filter.field}`).val(filter.value.max);
                        }
                    } else {
                        $('#filter-value-input').val(filter.value);
                    }

                    // Set operator
                    if (filterDef.operator.length > 1) {
                        $('#operator-select').val(filter.operator);
                        currentOperator = filter.operator;
                        updateDynamicInput(filterDef, currentOperator);
                    }
                }, 100);
            }
        });
    }

    // Get symbol for operator display
    function getOperatorSymbol(operator) {
        switch(operator) {
            case '=': return '=';
            case '!=': return '≠';
            case 'contains': return '⊇';
            case 'like': return '~';
            case '>': return '>';
            case '<': return '<';
            case '>=': return '≥';
            case '<=': return '≤';
            case '<>': return '≠';
            case 'range': return '';
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