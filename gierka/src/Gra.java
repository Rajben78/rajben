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
    private long czasOstatniegoPokonania; 
    private final ArrayList<EfektCzasteczkowy> efekty = new ArrayList<>();

    
    private boolean wcisnietyW, wcisnietyS, wcisnietyA, wcisnietyD;
    private boolean wcisnietyUp, wcisnietyDown, wcisnietyLeft, wcisnietyRight;

    public Gra() {
        this.setPreferredSize(new Dimension(1920, 1080)); 
        this.setBackground(Color.BLACK);
        this.addKeyListener(this);
        this.setFocusable(true);
        Font czcionka = new Font("Arial", Font.PLAIN, 40);

        
        graczX = 960; 
        graczY = 540; 
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

        
        for (int i = 0; i < 4; i++) {
            przeciwnicy.add(new Przeciwnik(losowy.nextInt(1920), losowy.nextInt(1080), 3 + losowy.nextInt(3)));
        }

        
        for (int i = 0; i < 8; i++) {
            przedmioty.add(new Przedmiot(losowy.nextInt(1920), losowy.nextInt(1080), losowy.nextInt(3)));
        }

        Timer licznikCzasu = new Timer(20, this);
        licznikCzasu.start();

        
        ZarzadcaDzwiekow.zaladujDzwieki();
        czasOstatniegoPokonania = System.currentTimeMillis(); 

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (wMenu) {
            
            g.setColor(Color.WHITE);
            g.drawString("Menu Pauzy - Kontynuuj (Enter) | Wyjdź (Esc)", 900, 500);
            return;
        }

        if (pauza) {
           
            g.setColor(Color.WHITE);
            g.drawString("Pauza - Wznów (Enter) | Wyjdź (Esc)", 900, 500);
            return;
        }

        g.setColor(Color.WHITE);
        g.fillRect(graczX, graczY, 20, 20); 

        
        for (Przeciwnik przeciwnik : przeciwnicy) {
            g.setColor(Color.RED);
            g.fillRect(przeciwnik.getX(), przeciwnik.getY(), 20, 20);
        }

        
        for (Przedmiot przedmiot : przedmioty) {
            if (przedmiot.getTyp() == 0) {
                g.setColor(Color.GREEN);
            } else if (przedmiot.getTyp() == 1) {
                g.setColor(Color.YELLOW); 
            } else {
                g.setColor(Color.CYAN); 
            }
            g.fillRect(przedmiot.getX(), przedmiot.getY(), 10, 10);
        }

        
        for (Pocisk pocisk : pociski) {
            g.setColor(Color.BLUE);
            g.fillRect(pocisk.getX(), pocisk.getY(), 5, 10);
        }

        
        for (EfektCzasteczkowy efekt : efekty) {
            efekt.rysuj(g);
        }

        
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

        
        if (wcisnietyW) {
            graczY -= predkoscGracza;  
        }
        if (wcisnietyS) {
            graczY += predkoscGracza;  
        }
        if (wcisnietyA) {
            graczX -= predkoscGracza;
        }
        if (wcisnietyD) {
            graczX += predkoscGracza;  
        }

        if (graczX < 0) graczX = 0;
        if (graczX > 1900) graczX = 1900; 
        if (graczY < 0) graczY = 0;
        if (graczY > 1060) graczY = 1060; 

       
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

               
                efekty.add(new EfektCzasteczkowy(przedmiot.getX(), przedmiot.getY(), Color.GREEN));

                przedmioty.remove(i);
                przedmioty.add(new Przedmiot(losowy.nextInt(1920), losowy.nextInt(1080), losowy.nextInt(3)));
                break;
            }
        }

       
        if (System.currentTimeMillis() - czasOstatniegoPokonania >= 3000 && przeciwnicy.size() > 0) {
           
            przeciwnicy.add(new Przeciwnik(losowy.nextInt(1920), losowy.nextInt(1080), 3 + losowy.nextInt(3)));
            czasOstatniegoPokonania = System.currentTimeMillis(); 
        }

       
        for (int i = 0; i < pociski.size(); i++) {
            Pocisk pocisk = pociski.get(i);
            pocisk.ruszaj();
            for (int j = 0; j < przeciwnicy.size(); j++) {
                Przeciwnik przeciwnik = przeciwnicy.get(j);
                if (new Rectangle(pocisk.getX(), pocisk.getY(), 5, 10).intersects(new Rectangle(przeciwnik.getX(), przeciwnik.getY(), 20, 20))) {
                    przeciwnicy.remove(j);
                    pociski.remove(i);
                    punkty += 100;

                    
                    efekty.add(new EfektCzasteczkowy(przeciwnik.getX(), przeciwnik.getY(), Color.RED));

                    ZarzadcaDzwiekow.odtworzDzwiekKill();

                   
                    czasOstatniegoPokonania = System.currentTimeMillis();

                    break;
                }
            }
        }

       
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

       
        if (klawisz == KeyEvent.VK_W) {
            wcisnietyW = true;  
        }
        if (klawisz == KeyEvent.VK_S) {
            wcisnietyS = true;
        }
        if (klawisz == KeyEvent.VK_A) {
            wcisnietyA = true;  
        }
        if (klawisz == KeyEvent.VK_D) {
            wcisnietyD = true; 
        }

      
        if (klawisz == KeyEvent.VK_UP) {
            wcisnietyUp = true;
            pociski.add(new Pocisk(graczX + 7, graczY, 10, -1));
        }
        if (klawisz == KeyEvent.VK_DOWN) {
            wcisnietyDown = true;
            pociski.add(new Pocisk(graczX + 7, graczY, 10, 1));  
        }
        if (klawisz == KeyEvent.VK_LEFT) {
            wcisnietyLeft = true;
            pociski.add(new Pocisk(graczX - 7, graczY, 10, -2));  
        }
        if (klawisz == KeyEvent.VK_RIGHT) {
            wcisnietyRight = true;
            pociski.add(new Pocisk(graczX + 7, graczY, 10, 2)); 
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int klawisz = e.getKeyCode();

       
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
    private ArrayList<Czasteczka> czasteczki; 

    public EfektCzasteczkowy(int x, int y, Color green) {
        this.x = x;
        this.y = y;
        this.czasZycia = 30; 
        this.czasteczki = new ArrayList<>();

        
        for (int i = 0; i < 50; i++) { 
            double kat = Math.random() * 2 * Math.PI; 
            double predkosc = 3 + Math.random() * 3;
            czasteczki.add(new Czasteczka(x, y, kat, predkosc));
        }
    }

    public void rysuj(Graphics g) {
      
        for (int i = 0; i < czasteczki.size(); i++) {
            Czasteczka czasteczka = czasteczki.get(i);
            czasteczka.ruszaj();
            czasteczka.rysuj(g);
        }

        
        if (czasZycia > 0) {
            czasZycia--;
        }
        if (czasZycia <= 0) {
            czasteczki.clear(); 
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

            
            this.predkoscX = Math.cos(kat) * predkosc;
            this.predkoscY = Math.sin(kat) * predkosc;

            
            this.kolor = new Color((float)Math.random(), (float)Math.random(), (float)Math.random());

            
            this.czasZycia = 20;
        }

        public void ruszaj() {
            x += predkoscX; 
            y += predkoscY;

            
            if (czasZycia > 0) {
                czasZycia--;
            }
        }

        public void rysuj(Graphics g) {
            if (czasZycia > 0) {
                g.setColor(kolor);
                g.fillOval(x, y, 5, 5); 
            }
        }
    }
}





class Przeciwnik {
    private int x, y, predkosc;
    private double kierunekX, kierunekY;  
    private Random losowy;

    public Przeciwnik(int x, int y, int predkosc) {
        this.x = x;
        this.y = y;
        this.predkosc = predkosc;
        this.losowy = new Random();
        
        this.kierunekX = losowy.nextDouble() * 2 - 1; 
        this.kierunekY = losowy.nextDouble() * 2 - 1; 
    }

    public void ruszaj() {
        
        double zmianaKierunku = 0.1;  
        kierunekX += (losowy.nextDouble() * zmianaKierunku * 2 - zmianaKierunku);  
        kierunekY += (losowy.nextDouble() * zmianaKierunku * 2 - zmianaKierunku);  

        
        if (kierunekX > 1) kierunekX = 1;
        if (kierunekX < -1) kierunekX = -1;
        if (kierunekY > 1) kierunekY = 1;
        if (kierunekY < -1) kierunekY = -1;

        
        x += kierunekX * predkosc;
        y += kierunekY * predkosc;

        
        if (x < 0) x = 0;
        if (x > 1900) x = 1900; 
        if (y < 0) y = 0;
        if (y > 1060) y = 1060; 
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
