# Sử dụng nginx làm base image
FROM nginx:alpine

# Copy file index.html vào thư mục mặc định của nginx
# Dockerfile tại fe/
COPY index.html /usr/share/nginx/html/

# Mở port 80 cho nginx
EXPOSE 80

# Sử dụng nginx để phục vụ các file tĩnh
CMD ["nginx", "-g", "daemon off;"]
