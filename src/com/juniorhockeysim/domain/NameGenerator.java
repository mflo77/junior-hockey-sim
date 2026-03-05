package com.juniorhockeysim.domain;

import java.util.Random;

public class NameGenerator {

    private static final Random random = new Random();

    private static final String[] FIRST_NAMES = {
        // North American classic
        "Tyler", "Ryan", "Connor", "Jake", "Liam", "Nathan", "Ethan", "Cole",
        "Dylan", "Mason", "Logan", "Hunter", "Braden", "Cody", "Travis",
        "Austin", "Jordan", "Derek", "Brady", "Reid", "Colton", "Carson",
        "Wyatt", "Lucas", "Owen", "Kyle", "Evan", "Seth", "Grant", "Tanner",
        "Drew", "Shane", "Brett", "Blake", "Luke", "Zach", "Cam", "Matt",
        "Nick", "Adam", "Alex", "Chris", "Eric", "Scott", "Justin", "Kevin",
        "Brandon", "Mike", "Jason", "Andrew", "Patrick", "Sean", "Brendan",
        "Ben", "Sam", "Max", "Oliver", "Noah", "William", "James", "Jack",
        "Henry", "Charlie", "Leo", "Finn", "Grayson", "Elijah", "Carter",
        "Jasper", "Rowan", "Miles", "Beckett", "Reid", "Nolan", "Tristan",
        "Declan", "Callum", "Griffin", "Rhett", "Silas", "Knox", "Crew",
        // Scandinavian
        "Lars", "Erik", "Mikael", "Niklas", "Joakim", "Henrik", "Victor",
        "Oscar", "Filip", "Sebastian", "Patrik", "Marcus", "Johan", "Axel",
        "Emil", "Lukas", "Elias", "Anton", "Simon", "Gabriel", "Felix",
        "Mats", "Markus", "Jonas", "Tobias", "Andreas", "Rasmus", "Jesper",
        "Viggo", "Soren", "Bjorn", "Stellan", "Leif", "Gunnar", "Torsten",
        // French/Francophone
        "Alexandre", "Maxime", "Olivier", "Alexis", "Nicolas", "Mathieu",
        "Antoine", "Philippe", "Francois", "Sebastien", "Julien", "Remi",
        "Cedric", "Florent", "Benoit", "Luc", "Yannick", "Etienne",
        // Finnish
        "Mikko", "Jari", "Teemu", "Pekka", "Esa", "Saku", "Tuukka", "Ville",
        "Juuso", "Olli", "Antti", "Valtteri", "Kaapo", "Tomi", "Arttu",
        "Eetu", "Jesperi", "Roope", "Sakari", "Hannu",
        // Russian/Eastern European
        "Andrei", "Dmitri", "Evgeni", "Ilya", "Ivan", "Kirill", "Mikhail",
        "Nikita", "Pavel", "Sergei", "Vladislav", "Alexei", "Artyom",
        "Konstantin", "Vyacheslav", "Yuri", "Boris", "Oleg",
        // Czech/Slovak
        "Jakub", "Ondrej", "Tomas", "Jiri", "Radek", "Marek", "David",
        "Martin", "Lukas", "Jan", "Michal", "Petr", "Zbynek", "Vaclav",
        // German/Other European
        "Moritz", "Nico", "Dominik", "Philipp", "Florian", "Stefan",
        "Miro", "Timo", "Yannick", "Luca", "Marco", "Mario", "Mattias",
        "Fabian", "Christoph", "Benedikt", "Hannes", "Valentin",
        // Baltic/Other
        "Andris", "Kristaps", "Rihards", "Rolands", "Martins", "Edgars",
        "Maris", "Janis", "Karlis", "Aigars",
        // Mixed/Modern
        "Zane", "Cruz", "Ryder", "Kade", "Jace", "Beau", "Colt", "Reef",
        "Sterling", "Lennon", "Archer", "Crew", "Blaze", "River", "Kaiden"
    };

    private static final String[] LAST_NAMES = {
        // English/Irish/Scottish
        "Smith", "Brown", "Wilson", "Taylor", "Anderson", "Thomas", "Jackson",
        "White", "Harris", "Martin", "Thompson", "Moore", "Allen", "Young",
        "Clark", "Lewis", "Walker", "Hall", "Robinson", "King", "Green",
        "Baker", "Mitchell", "Campbell", "Roberts", "Carter", "Phillips",
        "Evans", "Turner", "Scott", "Murphy", "Bailey", "Bell",
        "Cooper", "Reed", "Cook", "Morgan", "Edwards", "Peterson", "Hughes",
        "Foster", "Sanders", "Price", "Bennett", "Patterson", "Ward", "Coleman",
        "Fisher", "Hayes", "Griffin", "Richardson", "Cox", "Howard",
        "Johnston", "Murray", "McLean", "MacDonald", "MacLeod", "Stewart",
        "Fraser", "Duncan", "Henderson", "Grant", "Reid", "Ross", "Burns",
        "O'Brien", "O'Connor", "Gallagher", "Quinn", "Flynn", "Burke",
        "Callaghan", "Doyle", "Byrne", "Fitzgerald", "Connelly",
        // Scandinavian
        "Larsson", "Eriksson", "Lindqvist", "Nystrom", "Karlsson", "Bergman",
        "Johansson", "Holm", "Gustafsson", "Svensson", "Pettersson", "Nilsson",
        "Backstrom", "Lundqvist", "Forsberg", "Hedman", "Nylander", "Dahlin",
        "Bjork", "Rakell", "Rask", "Rinne", "Ekblad", "Persson", "Lindgren",
        "Magnusson", "Soderstrom", "Holmberg", "Bengtsson", "Carlsson",
        // Finnish
        "Lehkonen", "Barkov", "Laine", "Aho", "Kotkaniemi", "Virtanen",
        "Makinen", "Korhonen", "Heikkinen", "Niemi", "Mäkinen", "Hietanen",
        "Pelkonen", "Saarinen", "Jokinen", "Leinonen", "Haapala",
        // French/Quebecois
        "Dubois", "Leclair", "Bergeron", "Lapointe", "Tremblay", "Gagnon",
        "Deschamps", "Girard", "Couture", "Duchene", "Roy", "Savard",
        "Béliveau", "Lafleur", "Houde", "Bouchard", "Pelletier", "Ouellet",
        // Russian/Eastern European
        "Svechnikov", "Kucherov", "Tarasenko", "Panarin", "Kuznetsov",
        "Orlov", "Marchenko", "Provorov", "Zadorov", "Voronkov", "Bykov",
        "Makarov", "Petrov", "Khokhlachev", "Namestnikov", "Zibanezhad",
        // Czech/Slovak
        "Kowalski", "Novak", "Kovac", "Hruska", "Dvorak", "Krejci",
        "Hajek", "Cerny", "Kral", "Necas", "Chytil", "Pastrnak",
        "Kubalik", "Sedlak", "Vrana", "Kofent", "Jaskin", "Sobotka",
        // German/Swiss
        "Draisaitl", "Fiala", "Kurashev", "Niederreiter", "Streit",
        "Hauser", "Brandt", "Zimmermann", "Hofmann", "Schwarz",
        // Baltic
        "Balcers", "Girgensons", "Kulda", "Daugavins", "Balinskis",
        // Other distinctive
        "Park", "Kim", "Tanaka", "Kovacs", "Mironov", "Tolvanen",
        "Aberg", "Vatanen", "Granlund", "Puljujarvi", "Pohjanpalo",
        "Wennberg", "Ekman-Larsson", "Strome", "Manson", "Vatanen",
        "Lemieux", "Cournoyer", "Larose", "Seguin", "Marchand",
        "Reinhart", "Schenn", "Gostisbehere", "Sanheim", "Provorov"
    };

    public static String generate() {
        String first = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String last  = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        return first + " " + last;
    }
}
