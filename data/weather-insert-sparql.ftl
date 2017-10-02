PREFIX weather: <http://xmlns.com/weather/0.1/>
INSERT
{
	weather:${weatherForecast.city.id?c} a weather:City .
	weather:${weatherForecast.city.id?c} weather:cityName "${weatherForecast.city.name}" .
	<#list weatherForecast.list as forecast>
	weather:${forecast.dt?c} a weather:Forecast .
    weather:${forecast.dt?c} weather:temperature "${forecast.main.temp?c}" .
    weather:${forecast.dt?c} weather:description "${forecast.weather?first.description}" .
    weather:${forecast.dt?c} weather:city weather:${weatherForecast.city.id?c}<#sep> .</#sep>
	</#list>
}
WHERE {}