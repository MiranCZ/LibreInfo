FROM python:3.13-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY . .

EXPOSE 8090

CMD ["waitress-serve", "--host=0.0.0.0", "--port=8090", "app:server"]