import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.File;

public class Gra extends JPanel implements ActionListener, KeyListener {
    private int graczX, graczY, predkoscGracza, zycGracza, punkty, czas;
    private final ArrayList<Przeciwnik> przeciwnicy;
    private final ArrayList<Przedmiot> przedmioty;
    private final ArrayList<Pocisk> pociski;
    private final Random losowy;
    private boolean pauza, graSkonczoona, wMenu;
    private long czasOstatniegoPokonania; // Czas ostatniego pokonania przeciwnika
    private final ArrayList<EfektCzasteczkowy> efekty = new ArrayList<>();

    // Nowe zmienne do przechowywania stanu klawiszy
    private boolean wcisnietyW, wcisnietyS, wcisnietyA, wcisnietyD;
    private boolean wcisnietyUp, wcisnietyDown, wcisnietyLeft, wcisnietyRight;

    public Gra() {
        this.setPreferredSize(new Dimension(1920, 1080)); // Pełny ekran
        this.setBackground(Color.BLACK);
        this.addKeyListener(this);
        this.setFocusable(true);
        Font czcionka = new Font("Arial", Font.PLAIN, 40);

        // Gracz zaczyna na środku ekranu
        graczX = 960; // Środek ekranu szerokość 1920
        graczY = 540; // Środek ekranu wysokość 1080
        predkoscGracza = 5;
        punkty = 0;
        zycGracza = 3;
        czas = 0;
        przeciwnicy = new ArrayList<>();
        przedmioty = new ArrayList<>();
        pociski = new ArrayList<>();
        losowy = new Random();
        pauza = false;
        graSkonczoona = false;
        wMenu = false;

        // Tworzymy kilku przeciwników
        for (int i = 0; i < 4; i++) {
            przeciwnicy.add(new Przeciwnik(losowy.nextInt(1920), losowy.nextInt(1080), 3 + losowy.nextInt(3)));
        }

        // Tworzymy przedmioty, które gracz może zbierać
        for (int i = 0; i < 8; i++) {
            przedmioty.add(new Przedmiot(losowy.nextInt(1920), losowy.nextInt(1080), losowy.nextInt(3)));
        }

        Timer licznikCzasu = new Timer(20, this);
        licznikCzasu.start();

        // Załaduj dźwięki i muzykę w tle
        ZarzadcaDzwiekow.zaladujDzwieki();
        czasOstatniegoPokonania = System.currentTimeMillis(); // Na początku nie ma przeciwnika do odczekania

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (wMenu) {
            // Wyświetlanie menu
            g.setColor(Color.WHITE);
            g.drawString("Menu Pauzy - Kontynuuj (Enter) | Wyjdź (Esc)", 900, 500);
            return;
        }

        if (pauza) {
            // Komunikat o pauzie
            g.setColor(Color.WHITE);
            g.drawString("Pauza - Wznów (Enter) | Wyjdź (Esc)", 900, 500);
            return;
        }

        g.setColor(Color.WHITE);
        g.fillRect(graczX, graczY, 20, 20); // Gracz

        // Rysowanie przeciwników
        for (Przeciwnik przeciwnik : przeciwnicy) {
            g.setColor(Color.RED);
            g.fillRect(przeciwnik.getX(), przeciwnik.getY(), 20, 20);
        }

        // Rysowanie przedmiotów
        for (Przedmiot przedmiot : przedmioty) {
            if (przedmiot.getTyp() == 0) {
                g.setColor(Color.GREEN); // Zwykły przedmiot
            } else if (przedmiot.getTyp() == 1) {
                g.setColor(Color.YELLOW); // Przedmiot zwiększający punkty
            } else {
                g.setColor(Color.CYAN); // Przedmiot zwiększający prędkość
            }
            g.fillRect(przedmiot.getX(), przedmiot.getY(), 10, 10);
        }

        // Rysowanie pocisków
        for (Pocisk pocisk : pociski) {
            g.setColor(Color.BLUE);
            g.fillRect(pocisk.getX(), pocisk.getY(), 5, 10);
        }

        // Rysowanie efektów cząsteczkowych
        for (EfektCzasteczkowy efekt : efekty) {
            efekt.rysuj(g);
        }

        // Wyświetlanie punktów i żyć
        g.setColor(Color.WHITE);
        g.drawString("Punkty: " + punkty, 10, 10);
        g.drawString("Życia: " + zycGracza, 10, 30);
        g.drawString("Czas: " + czas / 1000, 10, 50);

        if (graSkonczoona) {
            g.drawString("Gra zakończona! Twoje punkty: " + punkty, 900, 500);
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (pauza || wMenu || graSkonczoona) {
            return;
        }

        czas += 20;

        // Ruch gracza
        if (wcisnietyW) {
            graczY -= predkoscGracza;  // Gracz porusza się w górę
        }
        if (wcisnietyS) {
            graczY += predkoscGracza;  // Gracz porusza się w dół
        }
        if (wcisnietyA) {
            graczX -= predkoscGracza;  // Gracz porusza się w lewo
        }
        if (wcisnietyD) {
            graczX += predkoscGracza;  // Gracz porusza się w prawo
        }

        // Ograniczenie ruchu gracza do ekranu
        if (graczX < 0) graczX = 0;
        if (graczX > 1900) graczX = 1900; // Zmieniona granica do pełnego ekranu
        if (graczY < 0) graczY = 0;
        if (graczY > 1060) graczY = 1060; // Zmieniona granica do pełnego ekranu

        // Ruch przeciwników
        for (Przeciwnik przeciwnik : przeciwnicy) {
            przeciwnik.ruszaj();
            if (new Rectangle(graczX, graczY, 20, 20).intersects(new Rectangle(przeciwnik.getX(), przeciwnik.getY(), 20, 20))) {
                zycGracza--;
                if (zycGracza == 0) {
                    graSkonczoona = true;
                    JOptionPane.showMessageDialog(this, "Koniec gry! Twoje punkty: " + punkty);
                    break;
                } else {
                    graczX = 960;
                    graczY = 540;
                    ZarzadcaDzwiekow.odtworzDzwiekSmierci();
                }
            }
        }

        // Sprawdzanie kolizji z przedmiotami
        for (int i = 0; i < przedmioty.size(); i++) {
            Przedmiot przedmiot = przedmioty.get(i);
            if (new Rectangle(graczX, graczY, 20, 20).intersects(new Rectangle(przedmiot.getX(), przedmiot.getY(), 10, 10))) {
                if (przedmiot.getTyp() == 0) {
                    punkty += 10;
                } else if (przedmiot.getTyp() == 1) {
                    punkty += 50;
                } else if (przedmiot.getTyp() == 2) {
                    predkoscGracza += 2;
                }

                // Tworzenie efektu cząsteczkowego przy zbieraniu przedmiotu
                efekty.add(new EfektCzasteczkowy(przedmiot.getX(), przedmiot.getY(), Color.GREEN));

                przedmioty.remove(i);
                przedmioty.add(new Przedmiot(losowy.nextInt(1920), losowy.nextInt(1080), losowy.nextInt(3)));
                break;
            }
        }

        // Sprawdzamy czas od pokonania przeciwnika, aby dodać nowego przeciwnika po 3 sekundach
        if (System.currentTimeMillis() - czasOstatniegoPokonania >= 3000 && przeciwnicy.size() > 0) {
            // Dodajemy nowego przeciwnika po zabiciu jednego
            przeciwnicy.add(new Przeciwnik(losowy.nextInt(1920), losowy.nextInt(1080), 3 + losowy.nextInt(3)));
            czasOstatniegoPokonania = System.currentTimeMillis(); // Zresetuj czas ostatniego pokonania
        }

        // Ruch pocisków
        for (int i = 0; i < pociski.size(); i++) {
            Pocisk pocisk = pociski.get(i);
            pocisk.ruszaj();
            for (int j = 0; j < przeciwnicy.size(); j++) {
                Przeciwnik przeciwnik = przeciwnicy.get(j);
                if (new Rectangle(pocisk.getX(), pocisk.getY(), 5, 10).intersects(new Rectangle(przeciwnik.getX(), przeciwnik.getY(), 20, 20))) {
                    przeciwnicy.remove(j);
                    pociski.remove(i);
                    punkty += 100;

                    // Dodajemy efekt cząsteczkowy przy zabiciu przeciwnika
                    efekty.add(new EfektCzasteczkowy(przeciwnik.getX(), przeciwnik.getY(), Color.RED));

                    ZarzadcaDzwiekow.odtworzDzwiekKill();

                    // Zapisz czas ostatniego pokonania przeciwnika
                    czasOstatniegoPokonania = System.currentTimeMillis();

                    break;
                }
            }
        }

        // Usuwanie nieaktywnych efektów cząsteczkowych
        efekty.removeIf(effekt -> effekt.czasZycia <= 0);

        repaint();
    }



    @Override
    public void keyPressed(KeyEvent e) {
        if (graSkonczoona) return;

        int klawisz = e.getKeyCode();

        if (pauza || wMenu) {
            if (klawisz == KeyEvent.VK_ENTER) {
                pauza = false;
                wMenu = false;
            } else if (klawisz == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }
            return;
        }

        // Sterowanie ruchem gracza
        if (klawisz == KeyEvent.VK_W) {
            wcisnietyW = true;  // Gracz chce iść w górę
        }
        if (klawisz == KeyEvent.VK_S) {
            wcisnietyS = true;  // Gracz chce iść w dół
        }
        if (klawisz == KeyEvent.VK_A) {
            wcisnietyA = true;  // Gracz chce iść w lewo
        }
        if (klawisz == KeyEvent.VK_D) {
            wcisnietyD = true;  // Gracz chce iść w prawo
        }

        // Sterowanie strzelaniem
        if (klawisz == KeyEvent.VK_UP) {
            wcisnietyUp = true;
            pociski.add(new Pocisk(graczX + 7, graczY, 10, -1));  // Strzał w górę
        }
        if (klawisz == KeyEvent.VK_DOWN) {
            wcisnietyDown = true;
            pociski.add(new Pocisk(graczX + 7, graczY, 10, 1));  // Strzał w dół
        }
        if (klawisz == KeyEvent.VK_LEFT) {
            wcisnietyLeft = true;
            pociski.add(new Pocisk(graczX - 7, graczY, 10, -2));  // Strzał w lewo
        }
        if (klawisz == KeyEvent.VK_RIGHT) {
            wcisnietyRight = true;
            pociski.add(new Pocisk(graczX + 7, graczY, 10, 2));  // Strzał w prawo
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int klawisz = e.getKeyCode();

        // Zwolnienie klawiszy
        if (klawisz == KeyEvent.VK_W) {
            wcisnietyW = false;
        }
        if (klawisz == KeyEvent.VK_S) {
            wcisnietyS = false;
        }
        if (klawisz == KeyEvent.VK_A) {
            wcisnietyA = false;
        }
        if (klawisz == KeyEvent.VK_D) {
            wcisnietyD = false;
        }

        if (klawisz == KeyEvent.VK_UP) {
            wcisnietyUp = false;
        }
        if (klawisz == KeyEvent.VK_DOWN) {
            wcisnietyDown = false;
        }
        if (klawisz == KeyEvent.VK_LEFT) {
            wcisnietyLeft = false;
        }
        if (klawisz == KeyEvent.VK_RIGHT) {
            wcisnietyRight = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame okno = new JFrame("Gra AI");
        Gra gra = new Gra();
        okno.setUndecorated(true);
        okno.setExtendedState(JFrame.MAXIMIZED_BOTH);
        okno.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        okno.add(gra);
        okno.setVisible(true);

        gra.requestFocusInWindow();
    }


}

class EfektCzasteczkowy {
    private int x, y;
    public int czasZycia;
    private ArrayList<Czasteczka> czasteczki; // Lista cząsteczek

    public EfektCzasteczkowy(int x, int y, Color green) {
        this.x = x;
        this.y = y;
        this.czasZycia = 30; // Efekt trwa 30 klatek
        this.czasteczki = new ArrayList<>();

        // Generowanie wielu cząsteczek w różnych kierunkach
        for (int i = 0; i < 50; i++) { // Tworzymy 50 cząsteczek
            double kat = Math.random() * 2 * Math.PI; // Losowy kąt w radianach
            double predkosc = 3 + Math.random() * 3; // Losowa prędkość cząsteczki
            czasteczki.add(new Czasteczka(x, y, kat, predkosc)); // Dodajemy cząsteczkę z losowym kątem i prędkością
        }
    }

    public void rysuj(Graphics g) {
        // Rysowanie cząsteczek
        for (int i = 0; i < czasteczki.size(); i++) {
            Czasteczka czasteczka = czasteczki.get(i);
            czasteczka.ruszaj();
            czasteczka.rysuj(g);
        }

        // Usuwanie cząsteczek po upływie czasu
        if (czasZycia > 0) {
            czasZycia--;
        }
        if (czasZycia <= 0) {
            czasteczki.clear(); // Usuwamy cząsteczki po zakończeniu życia efektu
        }
    }

    class Czasteczka {
        private int x, y;
        private double predkoscX, predkoscY;
        private int czasZycia;
        private Color kolor;

        public Czasteczka(int x, int y, double kat, double predkosc) {
            this.x = x;
            this.y = y;

            // Przekształcamy prędkość na składowe X i Y
            this.predkoscX = Math.cos(kat) * predkosc;
            this.predkoscY = Math.sin(kat) * predkosc;

            // Ustawiamy losowy kolor dla cząsteczki
            this.kolor = new Color((float)Math.random(), (float)Math.random(), (float)Math.random());

            // Cząsteczki będą żyły przez 20 klatek
            this.czasZycia = 20;
        }

        public void ruszaj() {
            x += predkoscX; // Zmieniamy pozycję cząsteczki
            y += predkoscY;

            // Cząsteczki zanikają po pewnym czasie
            if (czasZycia > 0) {
                czasZycia--;
            }
        }

        public void rysuj(Graphics g) {
            if (czasZycia > 0) {
                g.setColor(kolor);
                g.fillOval(x, y, 5, 5); // Rysowanie cząsteczki
            }
        }
    }
}





class Przeciwnik {
    private int x, y, predkosc;
    private double kierunekX, kierunekY;  // Dodajemy kierunki w postaci float, aby poruszali się bardziej płynnie
    private Random losowy;

    public Przeciwnik(int x, int y, int predkosc) {
        this.x = x;
        this.y = y;
        this.predkosc = predkosc;
        this.losowy = new Random();
        // Inicjujemy kierunek początkowy losowo w przedziale -1, 1
        this.kierunekX = losowy.nextDouble() * 2 - 1; // Losowy kierunek X (-1 do 1)
        this.kierunekY = losowy.nextDouble() * 2 - 1; // Losowy kierunek Y (-1 do 1)
    }

    public void ruszaj() {
        // Zmiana kierunku z pewną losowością dla płynności
        double zmianaKierunku = 0.1;  // Mała zmiana kierunku
        kierunekX += (losowy.nextDouble() * zmianaKierunku * 2 - zmianaKierunku);  // Zmiana w X
        kierunekY += (losowy.nextDouble() * zmianaKierunku * 2 - zmianaKierunku);  // Zmiana w Y

        // Utrzymujemy kierunek w przedziale [-1, 1] dla obu osi
        if (kierunekX > 1) kierunekX = 1;
        if (kierunekX < -1) kierunekX = -1;
        if (kierunekY > 1) kierunekY = 1;
        if (kierunekY < -1) kierunekY = -1;

        // Poruszamy przeciwnika w kierunku wyznaczonym przez kierunekX i kierunekY
        x += kierunekX * predkosc;
        y += kierunekY * predkosc;

        // Zapewniamy, że przeciwnik nie wyjdzie poza ekran
        if (x < 0) x = 0;
        if (x > 1900) x = 1900; // Zmieniona granica do pełnego ekranu
        if (y < 0) y = 0;
        if (y > 1060) y = 1060; // Zmieniona granica do pełnego ekranu
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}


class Przedmiot {
    private int x, y, typ;

    public Przedmiot(int x, int y, int typ) {
        this.x = x;
        this.y = y;
        this.typ = typ;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getTyp() {
        return typ;
    }
}

class Pocisk {
    private int x, y, predkosc, kierunek;

    public Pocisk(int x, int y, int predkosc, int kierunek) {
        this.x = x;
        this.y = y;
        this.predkosc = predkosc;
        this.kierunek = kierunek;
    }

    public void ruszaj() {
        if (kierunek == -1) {
            y -= predkosc;
        } else if (kierunek == 1) {
            y += predkosc;
        } else if (kierunek == -2) {
            x -= predkosc;
        } else if (kierunek == 2) {
            x += predkosc;
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

class ZarzadcaDzwiekow {
    private static Clip dzwiekSmierci;
    private static Clip dzwiekKill;
    private static Clip muzykaTla;

    public static void zaladujDzwieki() {
        dzwiekSmierci = zaladujClip("resources/ded.wav");
        dzwiekKill = zaladujClip("resources/kill.wav");
        muzykaTla = zaladujClip("resources/muzyka.wav");
        if (muzykaTla != null) {
            muzykaTla.loop(Clip.LOOP_CONTINUOUSLY);
            muzykaTla.start();
        }
    }

    private static Clip zaladujClip(String sciezkaDzwieku) {
        try {
            File dzwiek = new File(sciezkaDzwieku);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(dzwiek);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void odtworzDzwiekSmierci() {
        if (dzwiekSmierci != null) {
            dzwiekSmierci.setFramePosition(0);
            dzwiekSmierci.start();
        }
    }

    public static void odtworzDzwiekKill() {
        if (dzwiekKill != null) {
            dzwiekKill.setFramePosition(0);
            dzwiekKill.start();
        }
    }
}
