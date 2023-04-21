# RemoteAdmin
Narzędzie adminstracyjne pracujące w oparciu o protokół SSH służące do zdalnej synchronizachi poleceń na wielu stacjach roboczych.

## Kompilacja i uruchomienie

### Kompilacja:
<p><i>mvn clean</i></p>
<p><i>mvn install</i></p>

### Uruchomienie:
<i>java --module-path ./release  --add-modules javafx.controls,javafx.base,javafx.fxml -jar RemoteAdmin-1.5-jar-with-dependencies.jar </i>

[//]: # (<p>Polecenie: )

[//]: # (<i>./mvnw assembly:single</i>)

[//]: # (utworzy plik RemoteAdmin-1.5-jar-with-dependencies.jar ze wszystkimi zależnmi biblitekami &#40;za wyjątkiem JavaFX&#41;)

[//]: # (</p>)

[//]: # (<p>Polecenie: )

[//]: # (<i>./mvnw package</i>)

[//]: # (utworzy plik RemoteAdmin.jar dużo mniejszy ale bez dowiązań, które bedzie trzeba dodać.)

[//]: # (</p>)

[//]: # (Komenda uruchomienia JARa:)

[//]: # (<p>)

[//]: # (<i>java --module-path <b>/snap/openjfx/current/sdk/lib/</b> --add-modules javafx.controls,javafx.base,javafx.fxml -jar RemoteAdmin-1.5-jar-with-dependencies.jar</i>)

[//]: # (</p>)

## Historia Wersji:

### wersja 1.5.7
<li>Poprawki w pliku pom.xml dotyczące instalatora</li>
<li>Bardziej instuicyjne dodawanie komputerów</li>

### wersja 1.5.6
<li>Usunięcie blokowania przycisku polecenia ('>>') przy błędzie logowania.</li>
<li>Przycisk 'kopiuj do schowka'.</li>
<li>Obsługa wyłącznie aktywnej pracowni.</li>
<li>Zapamiętywanie zaznaczonych komputerów (bugfix).</li>
<li>Usunięte błędy przy 'batch connect' z wieloma komputerami, z których część nie jest responsywna.</li>
<li>Zamykanie wszystkich aktywnych wątków na koniec programu (bugfix).</li>

### wersja 1.5.5
<li>Podczas pierwszego uruchamiania programu program nie wysypuje się przy braku pliku <i>settings.json</i></li>

### wersja 1.5.3
<li>Obsługa polskich znaków diakrytycznych.</li>

### wersja 1.5.2
<li>Dialog hasła do sudo odblokowany</li>
<li>Naprawiony problem z przyciskiem 'połącz'</li>
<li>(Pozostaje problem polskich znaków)</li>

### wersja 1.5.1
<li>Korekta widoku pod Linux</li>

### wersja 1.5.0
<li>Poprawiony poważny błąd buforowania wyjścia na konsolę</li>
<li>skorygowane wszystkie uwagi za wyjątkiem punktów 2 i 14</li>
<li>Usunięte niepotrzebnie rozmnożone wątki </li>
<li>Poprawiony pasek aktywności aplikacji</li>
<li> usunięte liczne błędy</li>

### wersja 1.2.2
<li>Testowanie połączenia (ping, stan połaczenia sesji SSH)</li>
<li>Dodane pole do interakcji z programami zdalnymi.</li>
<li>Bardziej przejrzyste i poprawne komunikaty błęów/stanu maszyny.</li>
<li>Wznawianie zerwanych połaczeń</li>
<li>Większa stabilność porgramu</li>
<li>Usunięte wyłapane błędy</li>

### wersja 1.2.0
<li>poprawna obsługa konsoli wyjściowej</li>
<li>spersonalizowana historia wprowadzanych poleceń</li>
<li>przewijanie po historii poleceń klawiszami UP/DOWN</li>
<li>usuniętych mnóstwo błędów i nieprawidłowych zachowań programu</li>


### wersja 1.1.0
<li>dodawanie komputerów/pracowni</li>
<li>zapisywanie zmian w pliku data.json</li>
<li>walidacja wartości wpisywanych do pól edycji (filtrowanie)</li>
<li>Wyjustowany, wyrównany i poprawiony GUI</li>
<li>sporo drobnych poprawek</li>
<li>aktualizacja statusu maszyn w tabelce 'Pracownie komputerowe'</li>
<li>dodanie zapisu historii poleceń</li>

### wersja 1.0.3
Artefakt w out/artifacts/RemoteAdmin_jar/RemoteAdmin.jar
Komenda uruchomienia JARa:
<p>
<i>java --module-path <b>/snap/openjfx/current/sdk/lib/</b> --add-modules javafx.controls,javafx.base,javafx.fxml -jar RemoteAdmin.jar</i>
</p>
gdzie ścieżka: <b>/snap/openjfx/current/sdk/lib/</b>
oznaca ścieżkę do bibliotek javaFX (<i>javafx.controls.jar, javafx.base.jar i javafx.fxml.jar</i>)w systemie 

### wersja 1.0.2
Artefakt w out/artifacts/RemoteAdmin_jar/RemoteAdmin.jar

### wersja 1.0.0
<li>Program przydziela komputery do pracowni (aktualny plik danych - <b><i>data.json</i></b>).</li>
<li>Ustawienia maksymalnych czasów w zakładce ustawienia.</li>
<li>Dodana obsługa komendy <b><i>SUDO</b></i>.</li>
<li>Usunięcie drobnych błędów.</li>
<li>Dodana mnemoniczna lista błędów (opis znaczenia liczbowych wartości zakończenia operacji ExitStatus).</li>
<li><i>Program w niektórych konfiguracjach ma tendencję do "wieszania się", nie odblokowuje przyczisku >> oraz nie pokazuje progresu obok komputera. Faktycznie jednak po maksymalnym czasie ustawionym dla operacji przycisk się odblokowuje.</i></li>

### wersja beta 0.9.3
Poprawki w pliku POM.xml
### wersja beta 0.9.2
<li>Synchronizacja przycisku wydawania komend (CountDownLatch)</li>
<li>Dodana obsługa log4j</li>

### wersja beta 0.9.1
<li>Ignorowanie key finger print</li>
<li>Dodany pasek postępu dla operacji logowania</li>
<li>Usunięcie drobnych błędów przy łączeniu z klientami</li>

### wersja beta 0.9.0
<li>Przycisk połączenia przechodzi w stan DISABLED na czas łączenia/rozłączania.</li>

### wersja beta 0.8.0
<li>Dodane współbieżne logowanie/wylogowanie wraz z aktualizacją statusu operacji.</li>

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=PrzemyslawZagraniczny_RemoteAdmin&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=PrzemyslawZagraniczny_RemoteAdmin)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=PrzemyslawZagraniczny_RemoteAdmin&metric=bugs)](https://sonarcloud.io/summary/new_code?id=PrzemyslawZagraniczny_RemoteAdmin)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=PrzemyslawZagraniczny_RemoteAdmin&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=PrzemyslawZagraniczny_RemoteAdmin)
