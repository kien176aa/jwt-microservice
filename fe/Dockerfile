# Sử dụng nginx image hỗ trợ ARM64
FROM nginx:latest

# Copy file index.html vào thư mục mặc định của nginx
COPY index.html /usr/share/nginx/html/

# Mở port 80 cho nginx
EXPOSE 80

# Sử dụng nginx để phục vụ các file tĩnh
CMD ["nginx", "-g", "daemon off;"]