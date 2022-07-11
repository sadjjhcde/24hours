#Настройка окружения
1. Установить java

   sudo apt update

   sudo apt upgrade

   echo "deb http://ppa.launchpad.net/linuxuprising/java/ubuntu bionic main" | sudo tee /etc/apt/sources.list.d/linuxuprising-java.list

   sudo apt install dirmngr

   sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 73C3DB2A

   sudo apt update

   sudo apt install oracle-java17-installer

   sudo update-alternatives --config java

#Сборка и деплой

сборка: Maven - Lifecycle - Package, 24Server.jar падает в 24Server/target

создаем на сервере директорию (если нужно): mkdir -p /etc/24

заливаем 24Server.jar на сервер: scp "C:\Users\Mi\Google Диск\Проекты\24h\target\24Server.jar" root@151.248.115.89:/etc/24/
скачать лог: scp root@151.248.115.89:/etc/24/log.txt "C:\Users\Mi\Desktop"

запуск 24Server.jar: nohup java -Xmx1024m -jar /etc/24/24Server.jar > /etc/24/log.txt 2>&1 &
local: java -Xmx30724m -jar "C:\Users\Mi\Google Диск\Проекты\24h\target\24Server.jar" &

остановить: kill -9 <pid процесса>

определить pid процесса: ps -aux|grep java
