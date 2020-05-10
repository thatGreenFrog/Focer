package lv.greenfrog.crawler.classifier;

import java.io.File;

public class BinaryFocerClassifier extends AbstractFocerClassifier {

    public BinaryFocerClassifier(String resourceFolder) throws Exception {
        super(String.format("%s%sbinary", resourceFolder, File.separator));
    }

    @Override
    protected int getMaxNgram() {
        return 3;
    }

    public static void main(String[] args) throws Exception {
        new BinaryFocerClassifier("C:\\crawler\\resources").classify("Savukārt Veselības minsitre Ilze Viņķele paziņoja, ka VM jau šobrīd regulāri publicē aktuālo informāciju par pacientu izmeklējumiem, kas ziņojuši par koronavīrusa simptomiem. \n" +
                "\n" +
                "Preses brīfinga laikā SPKC Infekcijas slimību riska analīzes un profilakses departamenta direktors Jurijs Perevoščikovs paziņoja, ka lidmašīnā, kas Rīgā atlidoja no Turcijas un kurā bija ar koronavīrusu sasirgušais Irānas pilsonis, atradās 124 pasažieri. Tautjāts, cik ir bijis Latvijas valstspiederīgo šajā lidmāšīnā, Perevoščikovs nevarēja precizēt.   \n" +
                "\n" +
                "Tāpat VM valsts sekretāre Daina Mūrmane-Umbraško stāstīja, ka VM ir pieprasījusi atsevišķu finansējumu, lai nodrošinātu slimnīcas ar nepieciešamo papildus aprīkojumu. \n" +
                "\n" +
                "Savukārt Neatliekamās medicīnas un pacientu uzņemšanas klīnikas vadītājs Aleksejs Višņakovs, taujāts, vai būtu jāievieš papildus pasākumi lidostā \"Rīga\", piemēram, temperatūras mērīšana, lai novērstu infekcijas izplatību Latvijā, atbildēja, ka tas nebūtu efektīvākais risinājums. Viņš aicināja sabiedrību būt līdzatbildīgai un ziņot par simptomiem. Turklāt viņš norādīja, ka vīrusa inkubācijas periods ir 14 dienas. Līdz ar to simptomi var parādīties krietni vēlāk par ielidošanas dienu. \n" +
                "\n" +
                "\"Pārbaudīt temperatūru visās vietās ir bezjēdzīgi,\" komentēja Jurijs Perevoščikovs. Viņaprāt, temperatūras mērīšana rada viltus drošības sajūtu. \n" +
                "\n" +
                "Jau ziņots, ka trešdienas vakarā Igaunijā konstatēts pirmais jaunā koronavīrusa \"Covid-19\" gadījums, ceturtdienas rītā paziņoja sociālo lietu ministrs Tanels Kīks. Saslimušais cilvēks ar autobusu ieradies no Rīgas.\n" +
                "\n" +
                "\"Runa ir par Igaunijas pastāvīgo iedzīvotāju, kuram nav Igaunijas pilsonības un kurš ieradās valstī trešdienas vakarā,\" raidorganizācijai ERR pavēstīja Kīks.\n" +
                "\n" +
                "Pēc viņa teiktā, saslimušais ir Irānas pilsonis, kurš ieradies Igaunijā no savas dzimtenes, un viņš ievietots slimnīcā.");
    }
}
