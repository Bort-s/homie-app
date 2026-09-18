package com.example.homieapp.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.homieapp.R
import com.example.homieapp.model.Block
import com.example.homieapp.model.Guides
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GuidesViewModel : ViewModel() {
    private val _guides = MutableStateFlow(
        listOf(
            Guides(
                "Humedad Alta",
                1,
                "HUMEDAD",
                listOf(
                    Block(
                        title = "Causas",
                        content = "La humedad alta en el hogar suele originarse por una combinación de factores estructurales y hábitos cotidianos. Entre las causas más comunes se encuentran la condensación, que ocurre cuando el vapor de agua choca con superficies frías (como ventanas o paredes mal aisladas), y las filtraciones externas debidas a grietas o problemas en el tejado. Asimismo, actividades básicas como cocinar, ducharse con agua caliente o secar la ropa en el interior sin la ventilación adecuada pueden elevar drásticamente los niveles de higrometría en espacios cerrados.",
                    ),
                    Block(
                        title = "Consecuencias",
                        content = "Las consecuencias de un ambiente excesivamente húmedo afectan tanto a la integridad de la vivienda como a la salud de sus habitantes. A nivel estructural, es habitual la aparición de manchas de moho, desprendimiento de pintura y el deterioro de muebles de madera o textiles. En cuanto al bienestar personal, la proliferación de ácaros y hongos en el aire puede agravar problemas respiratorios, provocar alergias y aumentar la sensación de fatiga, además de dificultar la regulación térmica del cuerpo, haciendo que el frío se sienta mucho más intenso en invierno.",
                    ),
                    Block(
                        title = "Soluciones",
                        content = "Para solucionar este problema, la medida más eficaz y sencilla es" +
                                " garantizar una ventilación cruzada diaria, abriendo ventanas " +
                                "durante al menos 10 o 15 minutos. En estancias críticas como baños" +
                                " y cocinas, la instalación de extractores ayuda a evacuar el vapor " +
                                "de forma inmediata. Si el problema persiste, el uso de deshumidificadores " +
                                "eléctricos permite controlar el porcentaje de agua en el aire de manera " +
                                "precisa, mientras que la aplicación de pinturas térmicas o la mejora del " +
                                "aislamiento en paredes exteriores previene de raíz la formación de condensación.",
                    )

                )
            ),
            Guides(
                "Humedad Baja",
                2,
                "HUMEDAD",
                listOf(
                    Block(
                        title = "Causas",
                        content = "La humedad baja en el hogar surge principalmente por el uso excesivo de sistemas de calefacción o aire acondicionado, que eliminan el vapor de agua del ambiente, especialmente en climas secos o durante el invierno. Otras causas comunes incluyen vientos fuertes que resecan el interior a través de ventanas mal selladas, o la falta de fuentes de humedad natural como plantas o fuentes de agua. Además, materiales absorbentes como alfombras y muebles porosos pueden atrapar la poca humedad disponible, agravando la sequedad en espacios cerrados con poca ventilación húmeda.",
                    ),
                    Block(
                        title = "Consecuencias",
                        content = "Las consecuencias de la humedad baja afectan la salud respiratoria, causando irritación en garganta, nariz y ojos, sequedad en la piel y labios agrietados, e incrementando el riesgo de infecciones virales al facilitar la supervivencia de virus en el aire. En el hogar, daña maderas y pisos al provocar contracciones y grietas, mientras que la estática eléctrica genera molestias y riesgos para equipos electrónicos. A largo plazo, reduce la calidad del sueño y favorece la fatiga crónica por deshidratación ambiental.",
                    ),
                    Block(
                        title = "Soluciones",
                        content = "Para elevar la humedad baja, usa humidificadores ultrasónicos o evaporativos que liberen vapor controlado, manteniendo niveles entre 40-60%. Coloca recipientes con agua cerca de radiadores, cultiva plantas que transpiren humedad y evita el exceso de calefacción. Ventila en momentos húmedos del día y aplica bálsamos humectantes en piel expuesta, combinando estas medidas para un ambiente equilibrado y saludable.\n",
                    )
                )
            ),
            Guides(
                "Temperatura Alta",
                3,
                "TEMPERATURA",
                listOf(
                    Block(
                        title = "Causas",
                        content = "La temperatura alta en el hogar se origina por factores como la exposición directa al sol en ventanas sin protección, techos mal aislados que atrapan el calor exterior y electrodomésticos generadores de calor como hornos o computadoras. En climas cálidos, la falta de ventilación cruzada y el uso inadecuado de luces incandescentes elevan la sensación térmica. Además, hábitos como cocinar con gas o acumular objetos que obstruyen el flujo de aire contribuyen a un aumento progresivo de la temperatura interior.\n",
                    ),
                    Block(
                        title = "Consecuencias",
                        content = "Entre las consecuencias de la temperatura alta destacan golpes de calor, deshidratación y fatiga extrema en los habitantes, con riesgos mayores para niños y ancianos. Daña alimentos perecederos al acelerar su descomposición en refrigeradores sobrecargados, y acelera el desgaste de pinturas, muebles y aparatos electrónicos. También propicia plagas como cucarachas y moscas, elevando el consumo energético por aire acondicionado y reduciendo el confort general.\n",
                    ),
                    Block(
                        title = "Soluciones",
                        content = "Para controlar la temperatura alta, instala cortinas blackout o persianas reflectantes en ventanas, y usa ventiladores de techo para circular aire fresco. Aísla techos y paredes con materiales térmicos, opta por iluminación LED de bajo calor y ventila al atardecer cuando el exterior se enfría. Mantén electrodomésticos eficientes e hidrátate bebiendo agua fría, logrando un ambiente entre 20-26°C ideal para el bienestar.\n",
                    )
                )
            ),
            Guides(
                "Temperatura Baja",
                4,
                "TEMPERATURA",
                listOf(
                    Block(
                        title = "Causas",
                        content = "La temperatura baja en el hogar surge por corrientes de aire frío a través de ventanas y puertas mal selladas, techos o pisos sin aislamiento que permiten fugas térmicas hacia el exterior. En inviernos fríos o noches frescas, el uso insuficiente de calefacción y la acumulación de humedad congelada agravan la sensación de frío. Hábitos como ventilar excesivamente o bloquear radiadores con muebles también contribuyen a bajar la temperatura interior drásticamente.",
                    ),
                    Block(
                        title = "Consecuencias",
                        content = "Las consecuencias de la temperatura baja incluyen problemas circulatorios como manos y pies helados, rigidez muscular y mayor riesgo de resfriados o hipotermia en personas vulnerables. Daña tuberías al congelar el agua interior, causando rupturas costosas, y contrae materiales como maderas que se agrietan. Reduce la productividad, altera el sueño por incomodidad y eleva facturas de calefacción por sistemas ineficientes.",
                    ),
                    Block(
                        title = "Soluciones",
                        content = "Las consecuencias de la temperatura baja incluyen problemas circulatorios como manos y pies helados, rigidez muscular y mayor riesgo de resfriados o hipotermia en personas vulnerables. Daña tuberías al congelar el agua interior, causando rupturas costosas, y contrae materiales como maderas que se agrietan. Reduce la productividad, altera el sueño por incomodidad y eleva facturas de calefacción por sistemas ineficientes.",
                    )
                )
            ),
            Guides(
                "Gases Combustibles del Hogar",
                5,
                "CALIDAD DEL AIRE",
                listOf(
                    Block(
                        title = "Metano y Butano",
                        content = "gases combustibles como metano y propano en el hogar se origina en fugas de estufas de gas, calentadores defectuosos o tanques de propano mal conectados. El metano surge de desagües obstruidos o vertederos de residuos orgánicos cercanos, mientras que el propano escapa por válvulas flojas en cilindros o parrillas al aire libre almacenadas dentro. Hábitos como ignifugar sin ventilación o usar generadores en espacios cerrados elevan estos gases inodoros e inflamables.\n",
                    ),
                    Block(
                        title = "Soluciones",
                        content = "Para mitigar gases combustibles, instala detectores de metano y propano con alarmas audibles en cocinas y garajes. Inspecciona regularmente conexiones de gas con profesionales certificados, ventila espacios con extractores potentes y usa encendedores piezoeléctricos para evitar chispas. Almacena tanques de propano afuera, repara fugas inmediatamente y educa a la familia sobre evacuación rápida, asegurando aire limpio y seguro.\n",
                    )
                )
            ),
            Guides(
                "Compuestos Contaminantes del Aire",
                6,
                "CALIDAD DEL AIRE",
                listOf(
                    Block(
                        title = "COV, CO2, PM2.5",
                        content = "Las partículas contaminantes en el hogar, como PM2.5, CO2 y COV, se originan principalmente en actividades cotidianas y fuentes internas. El PM2.5 son partículas finas menores a 2.5 micras provenientes de humo de cigarrillos, cocción con leña o tráfico vehicular que se filtra, mientras que el CO2 se acumula por respiración humana, combustión incompleta de estufas y espacios mal ventilados. Los COV (compuestos orgánicos volátiles) provienen de pinturas recientes, aerosoles, adhesivos y productos de limpieza, elevando su concentración en ambientes cerrados sin renovación de aire.\n",
                    ),
                    Block(
                        title = "Soluciones",
                        content = "Para reducir partículas contaminantes, ventila diariamente abriendo ventanas opuestas para flujo cruzado y usa purificadores HEPA que capturan PM2.5 hasta 99.97%. Monitorea CO2 con medidores y activa extractores en baños/cocinas; elige productos bajos en COV y plantas como pothos para absorberlos naturalmente. Mantén filtros limpios en aires acondicionados y evita quema interna, logrando niveles seguros según guías OMS.\n",
                    ),
                )
            ),
        )
    )
    val listGuides: StateFlow<List<Guides>> = _guides.asStateFlow()
}