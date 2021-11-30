# Java Microservices with Spring Boot & Spring Cloud

Projekt systemu do składowania i przetwarzania obrazków

Architektura rozwiązania:
![image](https://github.com/krixe-dev/Image-Keeper-Public/blob/master/_img/diagram.png?raw=true)

* gateway-service - serwis udostępniający API do systemu, na adres tej usługi kierowane są żądania CRUD. Serwis sprawdza uprawnienia i przekazuje żądania dalej do usługi manager-service
* discovery-service - usługa discovery zapewniająca obsługę rozproszonego systemu i pozwalająca na wzajemne wykrywanie i rejestrowanie się mikroserwisów oraz jest odpowiedzialna za load-ballancing ruchu wewnątrz systemu miedzy serwisami
* download-service - serwis "publiczny", który umożliwia pobieranie obrazka z wykorzystaniem bezpiecznego, tymczasowego linku
* manager-service - serwis przyjmujący żądania z API i weryfikujący uprawnienia w dostepie do zasobów jakimi są informacje o przechowywanych obrazkach oraz same obrazki. Serwis pozwala na zarządzanie przechowywanymi zasobami tylko uwierzytelnionym użytkownikow w oparciu o ich role pozyskane z Keycloak
* storage-service - serwis obsługujący przetwarzanie obrazków. Przechowuje i zarządza metadanymi dotyczącymi grafik. Generuje bezpieczny, tymczasowy link do pobrania pliku

Dodatkowo w ramach systemu skonfigurowane są następujące komponenty
* baza danych PostgreSQL
* baza danych MongoDB
* kolejka komunikatów RabbitMQ
* system zarzadzania tożsamością Keycloak

<b>Przyjęte założenia</b>

1) Użytkownik dodaje obrazek do systemu (ADD IMAGE). Serwis 'manager-service' odpowiedzialny za weryfikację przychodzących żądań pod kątem biznesowym, przyjmuje obrazek, weryfikuje status uwierzytelnienia, odkłada plik we wspólnym file-system-ie i zwraca unikalny identyfikator obrazka. 
2) Użytkownik wykorzystując identyfikator obrazka może uzuełnić jego opis (ADD IMAGE DESCRIPTION) w każdej chwili może również pobrać szczegóły obrazka usługą GET IMAGE INFORMATION (ale do momentu zakończenia przetwarzania, nie będzie tam wszystkich danych)
3) Manager-service w między czasie wysyła powiadomienie (na kolejkę RabbitMQ) o nowym zarejestrowanym obrazku czekającym na przetworzenie.
4) Serwis 'storage-service' odbiera nową wiadomość z kolejki i przystępuje do "obliczenia" metadanych i wyliczenia skrótu pliku. W celu odczytu pliku wykorzystuje wspólny file-system. Dzięki temu nie ma konieczności przesyłania plików przez sieć.
5) Po zakończonym przetwarzaniu, storage-service wysyła wiadomość (na kolejkę RabbitMQ) o zakończonym przetwarzaniu.
6) Serwis manager-service odbiera komunikat i zmiena status obrazka na PRESENT (czyli obecny i gotowy)
7) Użytkownik może teraz pobierać informacje o obrazku z pełnym zakresem informacji (GET IMAGE INFORMATION) wraz z metadanymi
8) Metadane dla każdego obrazka są pobierane przez manager-service z serwisu storage-service za pomocą klienta REST (Feign client)
   TODO - należy dodać obsługę grupowania pobierania metadancyh w paczkach (szczególnie istotne w operacji GET ALL IMAGES)
9) Metadane raz pobranego obrazka są zapisywane w Cache (Hazelcast) przez manager-service, aby nie powielać niepotrzebnej komunikacji w obrebie ekosystemu mikroserwisów
   TODO - należy podłaczyć wykorzystanie Cache (Hazelcast) w operacjach odczytu danych z bazy przez manager-service
10) Uwierzytelniony użytkownik może w dowolnym momencie pobrać swój obrazek lub wygenerować bezpieczny publiczny link do pobrania obrazka i udostępnić go innym, nieuwierzytelnionym osobom
11) Podczas generowania unikalnego linku następuje komunikacja ze storage-service. Ten serwis tworzy unikalny link i zapisuje jego szczegóły dla konkretnego obrazka w swojej bazie MongoDB
12) Pobranie pliku przez niezalogowanego użytkownika jest możliwe przez 2 minuty od momentu wygenerowania linku. W tym celu należy wywołać usługę w serwisie download-service (DOWNLOAD IMAGE BY SECURE URL)
13) Serwis download-service w celu zdekodowania informacji z linku wywołuje storage-service by uzyskać identyfikator obrazka, który należy odczytać z systemu plików. Po uzyskaniu inforamcaji zwrotnej może przystąpić do odczytu pliku z dysku gdyż teraz ma jego nazwę.

# Adresy usług powiązanych, dostępnych w trakcie działania systemu

| Usługa | Adres | dane do logowania (login/hasło)
| --- | --- | --- |
| Discovery-service | http://localhost:8761 | --- |
| Keycloak | http://localhost:8080 | demo/demo |
| RabbitMQ | http://localhost:15672 | rabbitmq/rabbitmq |

Użytkownicy systemowi skonfigurowani w Keycloak (do wykorzyżstania w testach systemu)
| Nazwa | Hasło | Uprawnienia |
| --- | --- | --- |
| user1 | user1 | system_user |
| user2 | user2 | system_admin, system_user |

# Konfiguracja systemu

Aby uruchomić system należy posiadać:
* Docker Engine + Docker-Compose
* Java 8
* Maven 3

Aby pobrać kod, należy wykonać polecenia w konsoli
```bash
git clone https://github.com/krixe-dev/Image-Keeper-Public.git
cd Image-Keeper
```

Aby skompilować kod, nalezy wykonać polecenie (w katalogu z pobranymi źródłami)
```bash
mvn clean install
```

System można skalować przez zwiększenie liczby usług manager-service i storage-service
Aby postawić i skonfigurować środowisko należy skorzystać z Docker-Compose (po wcześniejszym skompilowaniu kodu)
- uruchomienie z jedną instancją manager-service i jedną instancją storage-service
```bash
docker-compose up -d --scale manager-service=1 --scale manager-service=1
```
Widok konsoli po wystartowaniu systemu:
```bash
[+] Running 10/10
 - Network services-network-01               Created 
 - Container discovery-service               Started 
 - Container postgres-database               Started
 - Container keycloak                        Started 
 - Container rabbitmq                        Started
 - Container mongo-database                  Started
 - Container download-service                Started
 - Container image-keeper-storage-service-1  Started
 - Container gateway-service                 Started 
 - Container image-keeper-manager-service-1  Started
```
- uruchomienie z dwiema instancjami manager-service i dwiema instancjami storage-service
```bash
docker-compose up -d --scale manager-service=2 --scale manager-service=2
```
- wyłączenie systemu
```bash
docker-compose down
```

# Jak testować i korzystać z systemu

W celu testowania usług należy pobrać i zainstalować aplikację Postman (ja wykorzystywałem wersję v9.2.0)

Projekt Postman z gotowymi CRUD znajduje się w katalogu: 
```bash
_postman
```
Wszystkie opisane tu usługi są już skonfigurowane w zakresie Uwierzytelnienia w projekcie Postman. 
Opis jak dokonać uwierzytelnienia znajduje się w dołączonej instrukcji - [Konfiguracja uwierzytelnienia OAuth2 w Postman.docx](https://github.com/krixe-dev/Image-Keeper-Public/blob/master/_postman/Konfiguracja%20uwierzytelnienia%20OAuth2%20w%20Postman.docx)

<b>Dostepne usługi API wraz z opisem:</b>

<details><summary><u>ADD IMAGE</u></summary>
<p>

usługa służy do wysłania nowego obrazka do systemu. 
W elemencie Body żądania należy dodać nowy element form-data

![image](https://github.com/krixe-dev/Image-Keeper-Public/blob/master/_img/add_image_1.png?raw=true)

1 - Wskazać konfigurację elementu Body, 2 - wybrać typ 'form-data', 3 - Wskazać, że podpinany będzie plik, 4 - wybrać plik z dysku

Adres usugi: 
<br/>
```bash
GET http://localhost:9000/images
```

Przykład prawidłowej odpowiedzi z usługi

```json
{
    "imageId": "339729e0-0690-4379-a08a-347289f50548",
    "status": "QUEUED",
    "owner": {
        "userName": "user1"
    },
    "createdOn": "2021-11-30T07:41:49.399+00:00",
    "updatedOn": "2021-11-30T07:41:49.399+00:00",
    "title": null,
    "description": null,
    "image-width": null,
    "image-height": null,
    "hash": null,
    "fileUrl": null,
    "instance": null
}
```
Nie wszystkie elementy odpowiedzi są uzupełnione. Część z nich wymaga dodania przez użytkownika (title, description) a część uzupełni się automatycznie, gdy system (storage-service) zakończy przetwarzanie pliku.

Najważniejsza informacja zwrotna to unikalny identyfikator obrazka w systemie
```json
"imageId": "339729e0-0690-4379-a08a-347289f50548",
```
Status 
```json
"status": "QUEUED",
```
oznacza, w jakim stanie znajduje się plik.</br>
Wszystkie możliwe statusy: 
```java
public enum ImageStatus {
    QUEUED, // zakolejkowany, czeka na przetworzenie i wyciągniecie metadanych
    PRESENT, // załadowany do systemu i gotowy do użycia
    CORRUPTED, // uszkodzony, nie możliwe jest jego przetwarzanie
    DELETED; // usunięty
}
```
Pozoztałe elementy odpowiedzi z systemu omówione zostaną przy okazji usługi GET IMAGE
</p>
</details>
<details><summary><u>ADD IMAGE DESCRIPTION</u></summary>
<p>

usługa służy do wysłania szczegółów przesłanego pliku. W celu poprawnego powiazania żądania z obrazkiem, 
którego opis chcemy zaktualizować, należy wykorzystać wartość 'imageId' uzyskaną w odpowiedzi na żądanie ADD IMAGE. 
Identyfikator należy dodać do ścieżki.

Adres usugi: 
<br/>
```bash
PUT http://localhost:8090/images/339729e0-0690-4379-a08a-347289f50548
```
W ciele żądania (Body) należy podać JSON z polami
```JSON
{
    "title" : "Image title",
    "description" : "Image description"
}
```
Przykład prawidłowej odpowiedzi z usługi
```JSON
{
    "imageId": "339729e0-0690-4379-a08a-347289f50548",
    "status": "QUEUED",
    "owner": {
        "userName": "user1"
    },
    "createdOn": "2021-11-30T07:41:49.399+00:00",
    "updatedOn": "2021-11-30T08:07:12.828+00:00",
    "title": "Image title",
    "description": "Image description",
    "image-width": null,
    "image-height": null,
    "hash": null,
    "fileUrl": null,
    "instance": null
}
```
Jak widać, nowe pola zostały uzupełnione (title, description)
<br/>
Zaktualizowała się również data ostatniej aktualizacji
```JSON
"updatedOn": "2021-11-30T08:07:12.828+00:00"
```
</p>
</details>
</details>
<details><summary><u>GET IMAGE INFORMATION</u></summary>
<p>

usługa służy do pobrania szczegółów konkretnego obrazka. W celu poprawnego powiazania żądania z obrazkiem, którego opis chcemy pobrać, należy wykorzystać wartość 'imageId' uzyskaną w odpowiedzi na żądanie ADD IMAGE i dodać ją do ścieżki

Adres usugi: 
<br/>
```bash
GET http://localhost:9000/images/339729e0-0690-4379-a08a-347289f50548
```
Przykład prawidłowej odpowiedzi z usługi
```JSON
{
    "imageId": "339729e0-0690-4379-a08a-347289f50548",
    "status": "PRESENT",
    "owner": {
        "userName": "user1"
    },
    "createdOn": "2021-11-30T07:41:49.399+00:00",
    "updatedOn": "2021-11-30T08:07:12.828+00:00",
    "title": "Image with title 11111",
    "description": "Image description",
    "image-width": 493,
    "image-height": 92,
    "hash": "cbe468abb8920998c003b49e29fa38ed3a963f932932774ac4dcdd632ee3896b",
    "fileUrl": "/images/339729e0-0690-4379-a08a-347289f50548/file",
    "instance": "Manager-Service:1ec37d4f-5c5c-429c-8fb3-bd865e6d8b7a"
}
```
<br/>
Opis pól:

| Pole | Opis | Metadane? |
| --- | --- | --- |
| imageId | Unikalny identyfikator obrazka | --- |
| status | Status obrazka w systemie | --- |
| owner | Właściciel (osoba która dodała obrazek do systemu | --- |
| createdOn | data dodania obrazka do systemu | --- |
| updatedOn | data ostatniej aktualizacji | --- |
| title | tytuł obrazka | --- |
| description | description | --- |
| image-width | wymiar obrazka - szerokość | tak |
| image-height | wymiar obrazka - wysokość | tak |
| hash | hask (SHA-256) obrazka | tak |
| fileUrl | link do pobrania obrazka (przez zalogowanego użytkownika) | --- |
| instance | nazwa instancji systemu, który zwrócił odpowiedź (w celu debugowania) | --- |

Metadane - określa, czy dane pochodzą z usługi storage-service i są dostępne po jego przetworzeniu 
</p>
</details>
<details><summary><u>GET IMAGE FILE</u></summary>
<p>

usługa służy do pobrania obrazka przez zalogowanego użytkwnika. Obrazek może pobrać TYLKO jego włąściciel i tylko gdy obrazek znajduje się w statusie PRESENT (czyli po jego przetworzeniu)

Adres usugi: 
<br/>
```bash
GET http://localhost:9000/images/339729e0-0690-4379-a08a-347289f50548/file
```
</p>
</details>
<details><summary><u>GET IMAGE SECURE URL</u></summary>
<p>

usługa służy do pobrania bezpiecznego linku do pobrania przez nieuwierzytelnionego użytkownika. W wyniku wywołania usługi zwrócony zostanie link ważny przez 2 minuty

Adres usugi: 
<br/>
```bash
GET http://localhost:9000/images/339729e0-0690-4379-a08a-347289f50548/url
```
Przykład prawidłowej odpowiedzi z usługi
```JSON
http://localhost:9010/download/2YdIZhOOhvbZ8J5OVkmBH8CxgjcI5WPUo4EeVSOyJfAm4qSX49kmaVHwWA9C6FFK
```
UWAGA!! - link do pobrania wskazuje na usługę z nowego serwisu - download-service. Ten serwis nie wymaga uwierzytelnienia. 
</p>
</details>
<details><summary><u>REMOVE IMAGE</u></summary>
<p>

usługa służy do usunięcia obrazka z systemu. W wyniku wywołania usługi zwrócony zostanie status 204 - No content. Jest to potwierdzenie poprawnego wywołania usługi

Adres usugi: 
<br/>
```bash
DELETE http://localhost:8090/images/339729e0-0690-4379-a08a-347289f50548
```
UWAGA!! - Usunięcie obrazka powoduje jedynie zmianę jego statusu. Możliwe jest nadal pozyskanie informacji o obrazku z usługi GET IMAGE INFORMATION. Nie jest już możliwa aktualizacja jego opisu ani pobranie.
</p>
</details>
<details><summary><u>DOWNLOAD IMAGE BY SECURE URL</u></summary>
<p>

usługa służy do pobrania obrazka z systemu przez niezalogowanego użytkownika. 
Link ważny jest 2 minuty od jego wygeneorwania.

Adres usugi: 
<br/>
```bash
GET http://localhost:9010/download/2YdIZhOOhvbZ8J5OVkmBH8CxgjcI5WPUo4EeVSOyJfAm4qSX49kmaVHwWA9C6FFK
```
UWAGA!! - Usunięcie obrazka powoduje jedynie zmiana jego statusu. 
</p>
</details>
<details><summary><u>GET ALL IMAGES</u></summary>
<p>

usługa służy do pobrania szczegółów wszystkich obrazków. Zwykły użytkownik pobierze informacje tylko o swoich obrazkach. Administrator pobierze informacje o wszystkich obrazkach.

Adres usugi: 
<br/>
```bash
GET http://localhost:9000/images
```
Przykład prawidłowej odpowiedzi z usługi
```JSON
[
    {
        "imageId": "339729e0-0690-4379-a08a-347289f50548",
        "status": "DELETED",
        "owner": {
            "userName": "user1"
        },
        "createdOn": "2021-11-30T07:41:49.399+00:00",
        "updatedOn": "2021-11-30T08:24:21.610+00:00",
        "title": "Image with title 11111",
        "description": "Image description",
        "image-width": null,
        "image-height": null,
        "hash": null,
        "fileUrl": null,
        "instance": "Manager-Service:8b24a0e4-ed3e-43b3-9a45-094949800e21"
    },
    {
        "imageId": "828b1d3b-a1c5-451f-adf1-d39ebeed0dd5",
        "status": "PRESENT",
        "owner": {
            "userName": "user2"
        },
        "createdOn": "2021-11-30T08:38:00.008+00:00",
        "updatedOn": "2021-11-30T08:38:00.008+00:00",
        "title": null,
        "description": null,
        "image-width": 552,
        "image-height": 777,
        "hash": "cbe468abb8920998c003b49e29fa38ed3a963f932932774ac4dcdd632ee3896b",
        "fileUrl": "/images/828b1d3b-a1c5-451f-adf1-d39ebeed0dd5/file",
        "instance": "Manager-Service:8b24a0e4-ed3e-43b3-9a45-094949800e21"
    }
]
```
</p>
</details>

# Co dalej

W projekcie należało by wykonać więcej usprawnień i mechanizmów, które będą wpływać na ergonomię rozwiązania.
* podpiąc serwer logów Zipkin
* dodać ograniczenia na wielkość przesłanego pliku
* dodać sensownego mechanizmu wyznaczania metadanych obrazka (wymiary obrazka są losowe)
* dodać fitrowanie i stronicowanie dla usługi pobierania informacji o wszystkich obrazkach GET ALL IMAGES
* dodać grupowanie żądań o metadane wysyłanych z manager-service do storage-service
* wykorzystać Cache (Hazelcast) przy operacjach na bazie danych w usługach manager-service
* dodać testy integracyjne
* obsługiwać dokładniej wyjątki w komunikacji pomiedzy manager-service i storage-service (w miejsce jednego zbiorczego typu błędu)