# RemoteAdmin
Narzędzie adminstracyjne pracujące w oparciu o protokół SSH służące do zdalnej synchronizachi poleceń na wielu stacjach roboczych.
## wersja 1.5.1
<li>Korekta widoku pod Linux</li>

## wersja 1.5.0
<li>Poprawiony poważny błąd buforowania wyjścia na konsolę</li>
<li>skorygowane wszystkie uwagi za wyjątkiem punktów 2 i 14</li>
<li>Usunięte niepotrzebnie rozmnożone wątki </li>
<li>Poprawiony pasek aktywności aplikacji</li>
<li> usunięte liczne błędy</li>

## wersja 1.2.2
<li>Testowanie połączenia (ping, stan połaczenia sesji SSH)</li>
<li>Dodane pole do interakcji z programami zdalnymi.</li>
<li>Bardziej przejrzyste i poprawne komunikaty błęów/stanu maszyny.</li>
<li>Wznawianie zerwanych połaczeń</li>
<li>Większa stabilność porgramu</li>
<li>Usunięte wyłapane błędy</li>

## wersja 1.2.0
<li>poprawna obsługa konsoli wyjściowej</li>
<li>spersonalizowana historia wprowadzanych poleceń</li>
<li>przewijanie po historii poleceń klawiszami UP/DOWN</li>
<li>usuniętych mnóstwo błędów i nieprawidłowych zachowań programu</li>


## wersja 1.1.0
<li>dodawanie komputerów/pracowni</li>
<li>zapisywanie zmian w pliku data.json</li>
<li>walidacja wartości wpisywanych do pól edycji (filtrowanie)</li>
<li>Wyjustowany, wyrównany i poprawiony GUI</li>
<li>sporo drobnych poprawek</li>
<li>aktualizacja statusu maszyn w tabelce 'Pracownie komputerowe'</li>
<li>dodanie zapisu historii poleceń</li>

## wersja 1.0.3
Artefakt w out/artifacts/RemoteAdmin_jar/RemoteAdmin.jar
Komenda uruchomienia JARa:
<p>
<i>java --module-path <b>/snap/openjfx/current/sdk/lib/</b> --add-modules javafx.controls,javafx.base,javafx.fxml -jar RemoteAdmin.jar</i>
</p>
gdzie ścieżka: <b>/snap/openjfx/current/sdk/lib/</b>
oznaca ścieżkę do bibliotek javaFX (<i>javafx.controls.jar, javafx.base.jar i javafx.fxml.jar</i>)w systemie 

## wersja 1.0.2
Artefakt w out/artifacts/RemoteAdmin_jar/RemoteAdmin.jar

## wersja 1.0.0
<li>Program przydziela komputery do pracowni (aktualny plik danych - <b><i>data.json</i></b>).</li>
<li>Ustawienia maksymalnych czasów w zakładce ustawienia.</li>
<li>Dodana obsługa komendy <b><i>SUDO</b></i>.</li>
<li>Usunięcie drobnych błędów.</li>
<li>Dodana mnemoniczna lista błędów (opis znaczenia liczbowych wartości zakończenia operacji ExitStatus).</li>
<li><i>Program w niektórych konfiguracjach ma tendencję do "wieszania się", nie odblokowuje przyczisku >> oraz nie pokazuje progresu obok komputera. Faktycznie jednak po maksymalnym czasie ustawionym dla operacji przycisk się odblokowuje.</i></li>

## wersja beta 0.9.3
Poprawki w pliku POM.xml
## wersja beta 0.9.2
<li>Synchronizacja przycisku wydawania komend (CountDownLatch)</li>
<li>Dodana obsługa log4j</li>

## wersja beta 0.9.1
<li>Ignorowanie key finger print</li>
<li>Dodany pasek postępu dla operacji logowania</li>
<li>Usunięcie drobnych błędów przy łączeniu z klientami</li>

## wersja beta 0.9.0
<li>Przycisk połączenia przechodzi w stan DISABLED na czas łączenia/rozłączania.</li>

## wersja beta 0.8.0
<li>Dodane współbieżne logowanie/wylogowanie wraz z aktualizacją statusu operacji.</li>

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=PrzemyslawZagraniczny_RemoteAdmin&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=PrzemyslawZagraniczny_RemoteAdmin)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=PrzemyslawZagraniczny_RemoteAdmin&metric=bugs)](https://sonarcloud.io/summary/new_code?id=PrzemyslawZagraniczny_RemoteAdmin)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=PrzemyslawZagraniczny_RemoteAdmin&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=PrzemyslawZagraniczny_RemoteAdmin)
