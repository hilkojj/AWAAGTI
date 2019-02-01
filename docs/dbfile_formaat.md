# .awaagti database formaat


## Introductie

Het .awaagti database formaat wordt gebruikt om weerdata op te slaan.
Het is een binair formaat.
Een .awaagti bestand representeerd 1 seconde aan weerdata.
In zo'n bestand staat voor die seconde de weerdata van elk station waarvoor voor die seconde data is gerapporteerd.
Het binair bestand is verdeelt in chunks, 1 chunk per weer station.
Het eerste byte in het bestand geeft aan hoeveel bytes elke chunk is.

De reden dat we losse bestanden hebben is om het sneller te maken om data op te vragen.
We hebben ge-experimenteerd met grotere bestanden waarbij niet het hele bestand ingelezen hoefde te worden, maar waarbij de lezer door indexes wist waar de benodigde data zich in het bestand bevind.
We hebben geleerd dat dit in Java geen merkbare snelheidswinst opleverd en dat het hebben van kleine bestanden de voorkeur heeft voor de snelheid van het lezen.
Hierbij ging het om queries waarbij een groot deel van de weerstations opgevraagd werd.
Het voordeel van zo'n systeem is wel dat minder schijfruimte benodigd is.

## Data formaat

Er zijn twee soorten manieren om weerdata op te slaan.
In een regulier weerdata database bestand bestaat een chunk uit:

 - 3 bytes voor de ID van het weerstation. Dit staat station IDs van 0 tot en met 16777215 toe.
 - 2 bytes aan temperatuur gemeten bij het weerstation. Dit is een positief geheel getal dat in tienden de graden celcius is opgeslagen, bij deze waarde is 1000 opgetelt waardoor van temperaturen van -100 tot 65435 graden celcius opgeslagen kunnen worden.
 - 1 byte aan wind snelheid in meter per seconde. Dit is een positief getal dat in tienden in m/s is opgeslagen, waardoor minimaal 0 en maximaal 25.5 meter per seconde kan worden opgeslagen. (optioneel)

Reguliere .awaagti bestanden hebben de tijd van de weerdata als bestandsnaam, in unix time.
Bijvoorbeeld 1549055732.awaagti, dat de gemeten weerdata bevat gemeten op die seconde.

### Summaries

Naast reguliere database bestanden zijn er zogenaamde summaries, waarbij een variable van elk weerstation in een tijdseenheid worden samenvat in 1 nieuwe waarde.
Summaries van de maximale en minimale waarde van de temperatuur en wind snelheid worden ondersteund.
Er zijn dus 4 verschillende summary files.

Een summary file wordt gegenereerd voor elke 100 seconden, en van elke honderd summary files wordt een nieuwe summary file genereerd, recursief.
De summary file 15490557 gaat over de (maximaal) honderd database files 1549055700 tot 1549055799.

In een summary file bestaat een chunk uit:

 - 3 bytes voor de ID van het weerstation.
 - 2 bytes voor de gegroepeerde waarde (bijvoorbeeld de max temperatuur).
 - 3 bytes voor de unix time stamp waarbij de waarde voor het station is gemeten.

Summary files hebben een bestandsnaam in het formaat '{variable}_{type summary}_sum.awaagti', bijvoorbeeld 'temp_max_sum.awaagti'.

## Directory structuur

In de database zijn de bestanden opgesplitst per honderd.
Hierdoor zijn er maximaal 100 database bestanden in een map, of 100 mappen in een map.
Hier is voor gekozen om het opvragen van de directory inhoud aan de file system van de kernel te versnellen.

Hier volgt als voorbeeld een schematische tekening van de directory inhoud:

```
15
    48
        00
            00
                 1548000000.awaagti
                 ...
                 1548000099.awaagti
                 temp_max_sum.awaagti
                 temp_min_sum.awaagti
                 wind_max_sum.awaagti
                  wind_min_sum.awaagti
             ...
             99
                 1548009901.awaagti
                 1548009999.awaagti
                 temp_max_sum.awaagti
                 temp_min_sum.awaagti
                 wind_max_sum.awaagti
                 wind_min_sum.awaagti
             temp_max_sum.awaagti
             temp_min_sum.awaagti
             wind_max_sum.awaagti
             wind_min_sum.awaagti
        ...
        99
	    ...
        temp_max_sum.awaagti
        temp_min_sum.awaagti
        wind_max_sum.awaagti
        wind_min_sum.awaagti
```

## Alternatieven

### Het comprimeren van een directory

We hebben gezocht naar manieren om schijfruimte te besparen.
De meest voor de hand liggende optie is om database bestanden te comprimeren.
Dat zou gedaan kunnen worden door de directory die 100 database bestanden bevat te comprimeren, en de originele directory te verwijderen.
Wanneer db_reader de directory nodig zou hebben, zou deze ge-oncomprimeerd kunnen worden in /tmp.
De directory '15/48/01/54/' zou dan worden vervangen met een bestand '15/48/01/54.zstd'.

In het geval dat dit idee wordt toegepast zal de compression methode aan een aantal eisen moeten voldoen.
 
 - Het gecomprimeerde bestand moet merkbaar kleiner zijn dan inhoud van de directory met database bestanden.
 - Het oncomprimeren moet ontzettend snel zijn, zodat het beantwoorden van queries van db_reader niet extra vertraging oploopt.

Er is gekozen om dit idee niet toe te passen in dit project.
We zijn niet tegen schijfruimte problemen aangelopen en het zou teveel tijd kosten om het idee uit te voeren.
