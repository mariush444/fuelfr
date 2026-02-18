#Fuel🇫🇷GPX

## Fuel FR prices converted to colored gpx for Osmand

Simple Android app that downloads open data from donnees.roulez-eco.fr then creates GPX file for Osmand where the price is repesented by color icon.

Prices for specified type of fuel are shown by colore icons from the cheapest to the most expensive: 
- light blue (cheapest) - from min price to min price + 5% (max price - min price) 
- dark green - min price + (from 5% to 10%) of the (max price - min price) 
- light green - min price + (from 10% to 20%) of the (max price - min price) 
- yellow - min price + (from 20% to 30%) of the (max price - min price) 
- orange - min price + (from 30% to 70%) of the (max price - min price) 
- red - min price + (from 70% to 80%) of the (max price - min price) 
- violet - min price + (from 80% to 90%) of the (max price - min price) 
- blue navy, dark blue (most expensive) - min price + 90% (max price - min price) to max price 

- black icon means that the type of fuel is not sold by the gas station.

### Functionality

1. black icons: 
- They can be switch from Osmamd view off as they are defind as separate group of icons called "Absence".
- User can excludes them from gpx file by unchecking option "Include stations without this fuel", so the file will be smaller.

2. Specified type of fuel are defined in the download file and it is: 
- Gazole
- SP95
- SP98
- E85
- E10 
- GPLc

User can choose which type of fuel should be ordered by color.

3. Name of the waypoint (gas stetion with color) conteins: 
- price of the specified type of fuel 
- date of the price 
- some services provided at gas station (represented by icon that I could find in utf emoticons)

4. Description of the waypoint contains:
- all prices of fuels and their dates that are sold at the gas station.
