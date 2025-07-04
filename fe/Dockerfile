# Sử dụng nginx:alpine làm base image
FROM nginx:alpine

# Copy file index.html, CSS và JS vào thư mục mặc định của nginx
COPY . /usr/share/nginx/html

# Mở port 80 cho nginx
EXPOSE 80

# Chạy nginx ở foreground để container không thoát
CMD ["nginx", "-g", "daemon off;"]