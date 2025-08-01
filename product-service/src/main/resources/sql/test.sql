-- COMPREHENSIVE PRODUCT PERFORMANCE ANALYSIS REPORT
-- Báo cáo phân tích hiệu suất sản phẩm toàn diện
-- Sử dụng 8 bảng: products, brands, categories, product_categories, order_items, reviews, wishlists, analytics

WITH
-- CTE 1: Core Product Information with Brand & Primary Category
product_base AS (
    SELECT
        p.id as product_id,
        p.name as product_name,
#         p.sku,
        p.price as list_price,
        p.status,
#         p.created_at as product_launch_date,
#         DATEDIFF(CURDATE(), p.created_at) as days_since_launch,

        -- Brand Information
#         b.id as brand_id,
#         b.name as brand_name,
#         b.is_active as brand_active,

        -- Primary Category Information
        c.id as category_id,
        c.name as category_name,
        c.parent_id as parent_category_id,
        pc.is_primary

    FROM products p
#              LEFT JOIN brands b ON p.brand_id = b.id
             LEFT JOIN product_categories pc ON p.id = pc.product_id AND pc.is_primary = true
             LEFT JOIN categories c ON pc.category_id = c.id
    WHERE p.status = 'active'
),

-- CTE 2: Sales Performance Metrics
sales_metrics AS (
    SELECT
        oi.product_id,
        COUNT(DISTINCT oi.order_id) as total_orders,
        SUM(oi.quantity) as total_units_sold,
        SUM(oi.total_price) as total_revenue,
        AVG(oi.unit_price) as avg_selling_price,
        MIN(oi.unit_price) as min_selling_price,
        MAX(oi.unit_price) as max_selling_price,

        -- Revenue by time periods
        SUM(CASE WHEN oi.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
                     THEN oi.total_price ELSE 0 END) as revenue_last_30_days,
        SUM(CASE WHEN oi.created_at >= DATE_SUB(CURDATE(), INTERVAL 90 DAY)
                     THEN oi.total_price ELSE 0 END) as revenue_last_90_days,
        SUM(CASE WHEN oi.created_at >= DATE_SUB(CURDATE(), INTERVAL 365 DAY)
                     THEN oi.total_price ELSE 0 END) as revenue_last_year,

        -- Units sold by time periods
        SUM(CASE WHEN oi.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
                     THEN oi.quantity ELSE 0 END) as units_sold_last_30_days,
        SUM(CASE WHEN oi.created_at >= DATE_SUB(CURDATE(), INTERVAL 90 DAY)
                     THEN oi.quantity ELSE 0 END) as units_sold_last_90_days,
        SUM(CASE WHEN oi.created_at >= DATE_SUB(CURDATE(), INTERVAL 365 DAY)
                     THEN oi.quantity ELSE 0 END) as units_sold_last_year,

        -- First and Last Sale
        MIN(oi.created_at) as first_sale_date,
        MAX(oi.created_at) as last_sale_date,
        DATEDIFF(CURDATE(), MAX(oi.created_at)) as days_since_last_sale

    FROM order_items oi
    GROUP BY oi.product_id
),

-- CTE 3: Customer Review & Rating Analysis
review_metrics AS (
    SELECT
        r.product_id,
        COUNT(*) as total_reviews,
        AVG(r.rating) as avg_rating,
        MIN(r.rating) as min_rating,
        MAX(r.rating) as max_rating,

        -- Rating distribution
        SUM(CASE WHEN r.rating = 5 THEN 1 ELSE 0 END) as rating_5_count,
        SUM(CASE WHEN r.rating = 4 THEN 1 ELSE 0 END) as rating_4_count,
        SUM(CASE WHEN r.rating = 3 THEN 1 ELSE 0 END) as rating_3_count,
        SUM(CASE WHEN r.rating = 2 THEN 1 ELSE 0 END) as rating_2_count,
        SUM(CASE WHEN r.rating = 1 THEN 1 ELSE 0 END) as rating_1_count,

        -- Review quality metrics
        COUNT(CASE WHEN r.is_verified_purchase = true THEN 1 END) as verified_reviews,
        COUNT(CASE WHEN r.is_approved = true THEN 1 END) as approved_reviews,
        AVG(r.helpful_count) as avg_helpful_count,
        SUM(r.helpful_count) as total_helpful_count,

        -- Recent review activity
        COUNT(CASE WHEN r.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) THEN 1 END) as reviews_last_30_days,
        COUNT(CASE WHEN r.created_at >= DATE_SUB(CURDATE(), INTERVAL 90 DAY) THEN 1 END) as reviews_last_90_days,

        -- Review timeline
        MIN(r.created_at) as first_review_date,
        MAX(r.created_at) as last_review_date

    FROM reviews r
    WHERE r.is_approved = true
    GROUP BY r.product_id
),

-- CTE 4: Wishlist & Interest Metrics
wishlist_metrics AS (
    SELECT
        w.product_id,
        COUNT(*) as total_wishlist_adds,
        COUNT(DISTINCT w.user_id) as unique_users_wishlisted,

        -- Wishlist activity by time
        COUNT(CASE WHEN w.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) THEN 1 END) as wishlist_adds_last_30_days,
        COUNT(CASE WHEN w.created_at >= DATE_SUB(CURDATE(), INTERVAL 90 DAY) THEN 1 END) as wishlist_adds_last_90_days,

        MIN(w.created_at) as first_wishlist_date,
        MAX(w.created_at) as last_wishlist_date

    FROM wishlists w
    GROUP BY w.product_id
),

-- CTE 5: Analytics & Traffic Metrics
traffic_metrics AS (
    SELECT
        a.product_id,
        SUM(a.views) as total_views,
        SUM(a.clicks) as total_clicks,
        AVG(a.conversion_rate) as avg_conversion_rate,
        AVG(a.bounce_rate) as avg_bounce_rate,

        -- Traffic by time periods
        SUM(CASE WHEN a.date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
                     THEN a.views ELSE 0 END) as views_last_30_days,
        SUM(CASE WHEN a.date >= DATE_SUB(CURDATE(), INTERVAL 90 DAY)
                     THEN a.views ELSE 0 END) as views_last_90_days,

        SUM(CASE WHEN a.date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
                     THEN a.clicks ELSE 0 END) as clicks_last_30_days,
        SUM(CASE WHEN a.date >= DATE_SUB(CURDATE(), INTERVAL 90 DAY)
                     THEN a.clicks ELSE 0 END) as clicks_last_90_days,

        -- Performance trends
        AVG(CASE WHEN a.date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
                     THEN a.conversion_rate END) as conversion_rate_last_30_days,
        AVG(CASE WHEN a.date >= DATE_SUB(CURDATE(), INTERVAL 90 DAY)
                     THEN a.conversion_rate END) as conversion_rate_last_90_days,

        COUNT(DISTINCT a.date) as days_with_data,
        MIN(a.date) as first_analytics_date,
        MAX(a.date) as last_analytics_date

    FROM analytics a
    WHERE a.product_id IS NOT NULL
    GROUP BY a.product_id
),

-- CTE 6: Category Performance Context
category_context AS (
    SELECT
        c.id as category_id,
        COUNT(DISTINCT pc.product_id) as products_in_category,
        AVG(sm.total_revenue) as category_avg_revenue,
        AVG(rm.avg_rating) as category_avg_rating,
        SUM(tm.total_views) as category_total_views
    FROM categories c
             JOIN product_categories pc ON c.id = pc.category_id
             LEFT JOIN sales_metrics sm ON pc.product_id = sm.product_id
             LEFT JOIN review_metrics rm ON pc.product_id = rm.product_id
             LEFT JOIN traffic_metrics tm ON pc.product_id = tm.product_id
    GROUP BY c.id
)

-- CTE 7: Brand Performance Context
# brand_context AS (
#     SELECT
#         b.id as brand_id,
#         COUNT(DISTINCT p.id) as products_in_brand,
#         AVG(sm.total_revenue) as brand_avg_revenue,
#         AVG(rm.avg_rating) as brand_avg_rating,
#         SUM(tm.total_views) as brand_total_views
#     FROM brands b
#              JOIN products p ON b.id = p.brand_id
#              LEFT JOIN sales_metrics sm ON p.id = sm.product_id
#              LEFT JOIN review_metrics rm ON p.id = rm.product_id
#              LEFT JOIN traffic_metrics tm ON p.id = tm.product_id
#     GROUP BY b.id
# )

-- MAIN QUERY: Comprehensive Product Performance Report
SELECT
    -- Product Identification
    pb.product_id,
    pb.product_name,
#     pb.sku,
#     pb.brand_name,
    pb.category_name,

    -- Product Lifecycle
#     pb.product_launch_date,
#     pb.days_since_launch,
#     CASE
#         WHEN pb.days_since_launch < 30 THEN 'New Launch'
#         WHEN pb.days_since_launch < 90 THEN 'Recent'
#         WHEN pb.days_since_launch < 365 THEN 'Established'
#         ELSE 'Mature'
#         END as product_lifecycle_stage,

    -- Pricing Analysis
    pb.list_price,
    COALESCE(sm.avg_selling_price, pb.list_price) as avg_selling_price,
    CASE
        WHEN sm.avg_selling_price IS NOT NULL
            THEN ROUND(((pb.list_price - sm.avg_selling_price) / pb.list_price * 100), 2)
        ELSE 0
        END as avg_discount_percentage,

    -- Sales Performance
    COALESCE(sm.total_orders, 0) as total_orders,
    COALESCE(sm.total_units_sold, 0) as total_units_sold,
    COALESCE(sm.total_revenue, 0) as total_revenue,
    COALESCE(sm.revenue_last_30_days, 0) as revenue_last_30_days,
    COALESCE(sm.revenue_last_90_days, 0) as revenue_last_90_days,
    COALESCE(sm.units_sold_last_30_days, 0) as units_sold_last_30_days,

    -- Sales Velocity & Trends
#     CASE
#         WHEN sm.total_units_sold IS NOT NULL AND pb.days_since_launch > 0
#             THEN ROUND(sm.total_units_sold / pb.days_since_launch, 2)
#         ELSE 0
#         END as avg_units_per_day_since_launch,

    CASE
        WHEN sm.revenue_last_30_days > 0 AND sm.revenue_last_90_days > 0
            THEN ROUND(((sm.revenue_last_30_days * 3 - sm.revenue_last_90_days) / sm.revenue_last_90_days * 100), 2)
        ELSE 0
        END as revenue_growth_trend_percentage,

    -- Customer Rating & Reviews
    COALESCE(rm.total_reviews, 0) as total_reviews,
    COALESCE(ROUND(rm.avg_rating, 2), 0) as avg_rating,
    COALESCE(rm.verified_reviews, 0) as verified_reviews,
    CASE
        WHEN rm.total_reviews > 0
            THEN ROUND(rm.verified_reviews * 100.0 / rm.total_reviews, 2)
        ELSE 0
        END as verified_review_percentage,

    -- Rating Distribution
    COALESCE(rm.rating_5_count, 0) as five_star_reviews,
    COALESCE(rm.rating_4_count, 0) as four_star_reviews,
    COALESCE(rm.rating_3_count, 0) as three_star_reviews,
    COALESCE(rm.rating_2_count, 0) as two_star_reviews,
    COALESCE(rm.rating_1_count, 0) as one_star_reviews,

    -- Customer Interest & Engagement
    COALESCE(wm.total_wishlist_adds, 0) as total_wishlist_adds,
    COALESCE(wm.unique_users_wishlisted, 0) as unique_users_wishlisted,
    COALESCE(wm.wishlist_adds_last_30_days, 0) as wishlist_adds_last_30_days,

    -- Traffic & Digital Performance
    COALESCE(tm.total_views, 0) as total_views,
    COALESCE(tm.total_clicks, 0) as total_clicks,
    CASE
        WHEN tm.total_views > 0
            THEN ROUND(tm.total_clicks * 100.0 / tm.total_views, 2)
        ELSE 0
        END as click_through_rate,

    COALESCE(ROUND(tm.avg_conversion_rate * 100, 2), 0) as avg_conversion_rate_percentage,
    COALESCE(ROUND(tm.avg_bounce_rate * 100, 2), 0) as avg_bounce_rate_percentage,

    -- Performance vs Category & Brand
    CASE
        WHEN cc.category_avg_revenue > 0 AND sm.total_revenue IS NOT NULL
            THEN ROUND((sm.total_revenue / cc.category_avg_revenue * 100), 2)
        ELSE 0
        END as revenue_vs_category_avg_percentage,

#     CASE
#         WHEN bc.brand_avg_revenue > 0 AND sm.total_revenue IS NOT NULL
#             THEN ROUND((sm.total_revenue / bc.brand_avg_revenue * 100), 2)
#         ELSE 0
#         END as revenue_vs_brand_avg_percentage,

    -- Engagement Metrics
    CASE
        WHEN sm.total_units_sold > 0 AND rm.total_reviews > 0
            THEN ROUND(rm.total_reviews * 100.0 / sm.total_units_sold, 2)
        ELSE 0
        END as review_rate_percentage,

    CASE
        WHEN tm.total_views > 0 AND wm.total_wishlist_adds > 0
            THEN ROUND(wm.total_wishlist_adds * 100.0 / tm.total_views, 2)
        ELSE 0
        END as wishlist_conversion_rate,

    -- Overall Performance Score (0-100)
    ROUND(
            (LEAST(COALESCE(sm.total_units_sold, 0) / 100, 1) * 25) +  -- Sales Score (25%)
            (LEAST(COALESCE(rm.avg_rating, 0) / 5, 1) * 20) +          -- Rating Score (20%)
            (LEAST(COALESCE(tm.total_views, 0) / 10000, 1) * 20) +     -- Traffic Score (20%)
            (LEAST(COALESCE(tm.avg_conversion_rate * 100, 0) / 10, 1) * 15) + -- Conversion Score (15%)
            (LEAST(COALESCE(wm.total_wishlist_adds, 0) / 50, 1) * 10) + -- Interest Score (10%)
            (LEAST(COALESCE(rm.total_reviews, 0) / 20, 1) * 10)        -- Review Volume Score (10%)
        , 2) as overall_performance_score,

    -- Performance Tier Classification
    CASE
        WHEN COALESCE(sm.total_revenue, 0) >= 10000 AND COALESCE(rm.avg_rating, 0) >= 4.5 THEN 'Star Performer'
        WHEN COALESCE(sm.total_revenue, 0) >= 5000 AND COALESCE(rm.avg_rating, 0) >= 4.0 THEN 'High Performer'
        WHEN COALESCE(sm.total_revenue, 0) >= 1000 AND COALESCE(rm.avg_rating, 0) >= 3.5 THEN 'Good Performer'
        WHEN COALESCE(sm.total_revenue, 0) >= 100 OR COALESCE(rm.avg_rating, 0) >= 3.0 THEN 'Average Performer'
        WHEN COALESCE(sm.total_revenue, 0) > 0 THEN 'Poor Performer'
        ELSE 'No Sales'
        END as performance_tier,

    -- Strategic Recommendations
    CASE
        WHEN COALESCE(sm.total_revenue, 0) = 0 AND COALESCE(tm.total_views, 0) > 100
            THEN 'High traffic but no sales - check pricing and conversion funnel'
        WHEN COALESCE(rm.avg_rating, 0) < 3.0 AND COALESCE(rm.total_reviews, 0) >= 5
            THEN 'Low ratings - focus on product quality improvement'
        WHEN COALESCE(sm.units_sold_last_30_days, 0) = 0 AND COALESCE(sm.total_units_sold, 0) > 0
            THEN 'No recent sales - consider promotion or inventory check'
        WHEN COALESCE(wm.total_wishlist_adds, 0) > COALESCE(sm.total_units_sold, 0) * 2
            THEN 'High wishlist interest - consider pricing strategy'
        WHEN COALESCE(tm.total_views, 0) > 1000 AND COALESCE(tm.avg_conversion_rate, 0) < 0.02
            THEN 'Low conversion rate - optimize product page'
        WHEN COALESCE(sm.total_revenue, 0) >= 5000 AND COALESCE(rm.total_reviews, 0) < 10
            THEN 'High sales but few reviews - encourage customer feedback'
        ELSE 'Monitor performance and maintain current strategy'
        END as strategic_recommendation

FROM product_base pb
         LEFT JOIN sales_metrics sm ON pb.product_id = sm.product_id
         LEFT JOIN review_metrics rm ON pb.product_id = rm.product_id
         LEFT JOIN wishlist_metrics wm ON pb.product_id = wm.product_id
         LEFT JOIN traffic_metrics tm ON pb.product_id = tm.product_id
         LEFT JOIN category_context cc ON pb.category_id = cc.category_id
#          LEFT JOIN brand_context bc ON pb.brand_id = bc.brand_id

ORDER BY
    overall_performance_score DESC,
    total_revenue DESC,
    avg_rating DESC,
    total_views DESC

-- Optional: Add LIMIT for top performers only
-- LIMIT 50;

-- ADDITIONAL SUMMARY STATISTICS
/*
UNION ALL

SELECT
    'SUMMARY STATISTICS' as product_name,
    '' as sku,
    '' as brand_name,
    '' as category_name,
    NULL as product_launch_date,
    NULL as days_since_launch,
    'TOTALS' as product_lifecycle_stage,
    AVG(pb.list_price) as list_price,
    AVG(sm.avg_selling_price) as avg_selling_price,
    0 as avg_discount_percentage,
    SUM(sm.total_orders) as total_orders,
    SUM(sm.total_units_sold) as total_units_sold,
    SUM(sm.total_revenue) as total_revenue,
    SUM(sm.revenue_last_30_days) as revenue_last_30_days,
    SUM(sm.revenue_last_90_days) as revenue_last_90_days,
    SUM(sm.units_sold_last_30_days) as units_sold_last_30_days,
    0 as avg_units_per_day_since_launch,
    0 as revenue_growth_trend_percentage,
    SUM(rm.total_reviews) as total_reviews,
    AVG(rm.avg_rating) as avg_rating,
    SUM(rm.verified_reviews) as verified_reviews,
    0 as verified_review_percentage,
    SUM(rm.rating_5_count) as five_star_reviews,
    SUM(rm.rating_4_count) as four_star_reviews,
    SUM(rm.rating_3_count) as three_star_reviews,
    SUM(rm.rating_2_count) as two_star_reviews,
    SUM(rm.rating_1_count) as one_star_reviews,
    SUM(wm.total_wishlist_adds) as total_wishlist_adds,
    SUM(wm.unique_users_wishlisted) as unique_users_wishlisted,
    SUM(wm.wishlist_adds_last_30_days) as wishlist_adds_last_30_days,
    SUM(tm.total_views) as total_views,
    SUM(tm.total_clicks) as total_clicks,
    0 as click_through_rate,
    AVG(tm.avg_conversion_rate) as avg_conversion_rate_percentage,
    AVG(tm.avg_bounce_rate) as avg_bounce_rate_percentage,
    0 as revenue_vs_category_avg_percentage,
    0 as revenue_vs_brand_avg_percentage,
    0 as review_rate_percentage,
    0 as wishlist_conversion_rate,
    AVG(overall_performance_score) as overall_performance_score,
    'OVERALL' as performance_tier,
    'See individual product recommendations' as strategic_recommendation

FROM product_base pb
LEFT JOIN sales_metrics sm ON pb.product_id = sm.product_id
LEFT JOIN review_metrics rm ON pb.product_id = rm.product_id
LEFT JOIN wishlist_metrics wm ON pb.product_id = wm.product_id
LEFT JOIN traffic_metrics tm ON pb.product_id = tm.product_id
*/