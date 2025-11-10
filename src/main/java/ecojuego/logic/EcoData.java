package src.main.java.ecojuego.logic;

import java.util.List;
import java.util.Map;

public final class EcoData {

    private EcoData() {
    }

    public static List<EcoItem> sampleItems() {
        return EcoCatalogStore.load();
    }

    public static Map<Category, List<String>> tips() {
        return Map.of(
            Category.ORGANICO, List.of(
                "Los residuos organicos pueden convertirse en compost si los separas correctamente.",
                "Guarda los restos organicos separados de los reciclables para evitar contaminarlos.",
                "Una compostera casera reduce hasta un 40 por ciento de la basura domestica."
            ),
            Category.RECICLABLE, List.of(
                "Enjuaga los envases reciclables para que no contaminen otros materiales.",
                "Carton y papel deben estar secos y limpios para poder reciclarse.",
                "Compacta latas y botellas para ahorrar espacio en el contenedor."
            ),
            Category.PELIGROSO, List.of(
                "Nunca deseches pilas o baterias en la basura comun; llevalas a un punto autorizado.",
                "Los medicamentos vencidos requieren un manejo especial para evitar contaminar el agua.",
                "Guarda aceites o quimicos en envases cerrados antes de llevarlos a un centro especializado."
            ),
            Category.DESCONOCIDO, List.of(
                "Separar los residuos facilita su reutilizacion y protege el ambiente.",
                "Clasificar correctamente evita que materiales valiosos terminen en vertederos."
            )
        );
    }

    public static List<EcoItem> defaultItems() {
        return List.of(
            new EcoItem("cascara de banano", Category.ORGANICO, "Los restos de frutas se descomponen rapidamente y se convierten en abono natural.", "Depositalas en el contenedor organico o en tu compostera domestica."),
            new EcoItem("manzana podrida", Category.ORGANICO, "La fruta en mal estado es materia organica rica en nutrientes para el suelo.", "Aprovecha el compost o lleva el residuo al contenedor marron de organicos."),
            new EcoItem("restos de comida cocida", Category.ORGANICO, "Los sobrantes de alimentos aportan humedad y nitrogeno al compost.", "Retira huesos y lleva lo restante al contenedor organico o a la compostera."),
            new EcoItem("cascaron de huevo", Category.ORGANICO, "Las cascaras de huevo aportan calcio y se degradan en el sustrato.", "Trituralas ligeramente y anadelas al contenedor organico o a tu compost."),
            new EcoItem("hojas secas", Category.ORGANICO, "Las hojas secas equilibran la mezcla de carbono en el compost.", "Agrega las hojas a una bolsa de residuos verdes o a la compostera."),
            new EcoItem("posos de cafe", Category.ORGANICO, "El cafe usado mejora la aireacion y aporta nitrogeno.", "Dejalos enfriar y colocalos en el contenedor organico o mezclados en la tierra."),
            new EcoItem("bolsa de te usada", Category.ORGANICO, "Las infusiones son residuos biodegradables que enriquecen el compost.", "Si la bolsa es de papel o fibra natural, colocala en el organico; retira grapas si las tuviera."),
            new EcoItem("pan duro", Category.ORGANICO, "El pan es materia organica que se degrada sin problemas.", "Cortalo en trozos y llevalo al contenedor marron o compostera."),
            new EcoItem("cascara de naranja", Category.ORGANICO, "Las citricas aportan aceites naturales que ahuyentan insectos en el compost.", "Cortalas en trozos pequenos y mezclalas con material seco."),
            new EcoItem("restos de ensalada", Category.ORGANICO, "Las verduras frescas agregan humedad y nitrogeno balanceado.", "Escurre salsas muy grasosas y depositalos en el contenedor organico."),
            new EcoItem("residuos de poda fina", Category.ORGANICO, "Las ramas pequenas airean el compost y evitan malos olores.", "Tritura ligeramente las ramas y llevalas al punto de residuos verdes."),
            new EcoItem("filtro de cafe compostable", Category.ORGANICO, "Los filtros de papel sin blanquear se degradan junto con el cafe usado.", "Exprime el exceso de liquido y colocalo en el contenedor marron."),
            new EcoItem("caja de pizza sin grasa", Category.ORGANICO, "El carton con residuos de comida deja de ser reciclable y funciona como carbono en el compost.", "Corta la caja y mezclala con residuos humedos."),
            new EcoItem("botella plastica transparente", Category.RECICLABLE, "El PET es 100 por ciento reciclable y se transforma en nuevos envases o fibras.", "Enjuaga la botella, aplastala y depositala en el contenedor amarillo o de plasticos."),
            new EcoItem("envase de vidrio de mermelada", Category.RECICLABLE, "El vidrio se recicla infinitas veces sin perder calidad.", "Quita la tapa metalica y deposita el frasco limpio en el contenedor verde de vidrio."),
            new EcoItem("lata de refresco", Category.RECICLABLE, "El aluminio reciclado ahorra hasta 95 por ciento de la energia frente a producirlo nuevo.", "Compacta ligeramente la lata y colocala en el contenedor amarillo."),
            new EcoItem("caja de carton de embalaje", Category.RECICLABLE, "El carton puede reciclarse multiples veces para fabricar papel nuevo.", "Dobla la caja para ahorrar espacio y llevala al contenedor azul."),
            new EcoItem("revista usada", Category.RECICLABLE, "El papel impreso es reciclable si esta limpio y seco.", "Agrupa las revistas y depositalas en el contenedor azul."),
            new EcoItem("botella de detergente", Category.RECICLABLE, "Los envases de HDPE o PP tienen un circuito estable de reciclaje.", "Aclara el envase, cierra la tapa y depositalo en el contenedor amarillo."),
            new EcoItem("lata de conserva", Category.RECICLABLE, "El acero se recupera facilmente mediante imanes en las plantas de reciclaje.", "Retira restos de comida, aplana si puedes y llevala al contenedor amarillo."),
            new EcoItem("tetrabrik de leche", Category.RECICLABLE, "Los envases multicapa se reciclan para crear carton y plasticos secundarios.", "Enjuaga el envase, pliegalo y depositalo en el contenedor amarillo."),
            new EcoItem("bolsas de plastico hdpe", Category.RECICLABLE, "Las bolsas gruesas pueden reciclarse para fabricar madera plastica.", "Llevalas limpias al contenedor de plasticos o puntos de acopio especiales."),
            new EcoItem("frascos cosmeticos de vidrio", Category.RECICLABLE, "El vidrio cosmético es apto para reciclaje si se entrega sin residuos.", "Retira la bomba o tapa plastica y deposita el frasco limpio en el contenedor verde."),
            new EcoItem("lata de comida para mascotas", Category.RECICLABLE, "El aluminio o acero de estas latas tiene alta recuperacion.", "Enjuaga la lata y depositala junto a otros metales reciclables."),
            new EcoItem("bandeja de poliestireno limpia", Category.RECICLABLE, "El poliestireno expandido se compacta para fabricar nuevos plasticos.", "Retira restos de comida y entrégala en centros que reciban telgopor."),
            new EcoItem("papel kraft sin tinta", Category.RECICLABLE, "El papel natural mantiene fibras largas ideales para reciclar.", "Dobla el papel y colocarlo en el contenedor azul, siempre seco."),
            new EcoItem("bateria aa", Category.PELIGROSO, "Las baterias contienen metales pesados que contaminan agua y suelo.", "Guardalas en un contenedor seguro y llevalas a un punto limpio o recolector de pilas."),
            new EcoItem("pilas boton", Category.PELIGROSO, "Las pilas boton concentran mercurio y requieren tratamiento especializado.", "Almacenalas en un frasco cerrado y entregalas en centros autorizados."),
            new EcoItem("aceite de cocina usado", Category.PELIGROSO, "El aceite vertido obstruye tuberias y contamina rios.", "Reune el aceite en un envase plastico y llevalo a un punto de recogida de aceite usado."),
            new EcoItem("termometro de mercurio roto", Category.PELIGROSO, "El mercurio es toxico y se evapora facilmente.", "No toques el mercurio, ventila el lugar y entrega el termometro en un punto limpio."),
            new EcoItem("bombilla fluorescente", Category.PELIGROSO, "Las lamparas contienen mercurio y componentes electronicos.", "Guarda la bombilla intacta en su caja y llevala a un punto de reciclaje de luminarias."),
            new EcoItem("frasco de medicamento", Category.PELIGROSO, "Los envases de farmacos pueden contener restos quimicos activos.", "Entregalos en campanas de devolucion o en la farmacia para su gestion segura."),
            new EcoItem("aerosol de pintura", Category.PELIGROSO, "Los aerosoles conservan gases presurizados y solventes inflamables.", "No perforar. Llevalo a un punto limpio o centro de residuos peligrosos."),
            new EcoItem("pilas recargables danadas", Category.PELIGROSO, "Las baterias recargables incluyen litio y niquel con riesgo de incendio.", "Colocalas en un recipiente aislado y entregalas en puntos de reciclaje de baterias."),
            new EcoItem("toner de impresora usado", Category.PELIGROSO, "El polvo del toner contiene particulas finas y pigmentos que contaminan.", "Sellalo en su bolsa original y entregalo en centros de gestion tecnologica."),
            new EcoItem("aceite de motor", Category.PELIGROSO, "Los lubricantes tienen aditivos toxicos y metales pesados.", "Guarda el aceite en un envase hermetico y lleva al punto limpio automotriz."),
            new EcoItem("pesticida domestico vencido", Category.PELIGROSO, "Los pesticidas mantienen activos quimicos peligrosos para la salud.", "No los vacies. Entregalos en campañas municipales de residuos peligrosos."),
            new EcoItem("disolvente para pintura", Category.PELIGROSO, "Los solventes emiten vapores inflamables y contaminantes.", "Mantelos en su envase original y entrégalos en centros especializados."),
            new EcoItem("termometro digital con bateria", Category.PELIGROSO, "Contiene baterias boton y componentes electronicos.", "Depositalo en un punto limpio electrónico para su desmontaje seguro.")
        );
    }
}
