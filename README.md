# RemoteAdmin
Narzędzie adminstracyjne pracujące w oparciu o protokół SSH służące do zdalnej synchronizachi poleceń na wielu stacjach roboczych.
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
