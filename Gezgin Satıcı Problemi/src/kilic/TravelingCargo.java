/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kilic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;

/**
 *
 * @author askilic
 */
public class TravelingCargo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        int n = 0;//Bu sıfırdan büyük olmalı ve 10'dan küçük olmalı
        System.out.println("Lütfen kaç şehire gitmek istediğinizi giriniz:(0-10)");
        while (true) {
            n = scan.nextInt();
            if (n > 0 && n <= 10) {
                break;
            } else {
                System.out.println("şehir sayısı geçersiz.Lutfen tekrar giriniz!!!");
            }
        }

        List<Integer> toVisitedCities = new ArrayList<>();
        toVisitedCities.add(40); //Kocaeli

        System.out.print("Lütfen bu şehirlerin plaka numaralarını  \n");
        
        for (int i = 0; i < n; i++) {

            int cityPlateNumber = scan.nextInt()-1;

            //Aynı şehiri birden fazla kez girebilir.Eğer girilmezse onu ayrı bir döngü ile kontrol edebilirsin.
            if (!(cityPlateNumber >= 0 && cityPlateNumber < 81 && cityPlateNumber != 40)) {
                System.out.println("Böyle bir şehir yoktur.Lütfen aynı şehir bilgisini doğru giriniz.");
                i--;//Bir sonraki isleme gecmis oldu.
            }
            else
            toVisitedCities.add(cityPlateNumber);
        }

        toVisitedCities.add(40); //Go to home...

        ShortestPath sp = new ShortestPath(toVisitedCities);
        sp.run();
        
        //Dosya yazma islemi gerceklestirilir
        File file = new File("output.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(TravelingCargo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(file, false);
            BufferedWriter bWriter = new BufferedWriter(fileWriter);
            bWriter.write("Travel Best Path: ");
        for (int i = 0; i < sp.bestTravelPathList.size(); ++i) {

            List<Integer> tmp = sp.bestTravelPathList.get(i);

            for (int j = tmp.size() - 1; j > 0; --j) {
                String s = Integer.toString(tmp.get(j)+1) + " - ";
                bWriter.write(s);
            }
        }
         bWriter.write("41"); //Add to home
         bWriter.write("\n\n");

        bWriter.write("Travel Total Cost: " + sp.totalTravelCost + "\n");
        bWriter.write("\n\n\t**********\t\n\n");
        
        
        int altRouteCost = 0;
        bWriter.write("Alternatif Travel Path: ");
        //alt yolu yazdırır
        for (int i = 0; i < sp.altTravelPathList.size() / 2; ++i) {

            List<Integer> tmp = sp.altTravelPathList.get(i * 2);

            int d = i;
            int m = 0;

            altRouteCost += sp.alternativeSolutions.get(d).get(m).totalCost;

            for (int j = tmp.size() - 1; j > 0; --j) {
                 String s = Integer.toString(tmp.get(j)+1) + " - ";
                bWriter.write(s);
            }
        }

        bWriter.write("41"); //Add to home
        bWriter.write("\n\n");
        
        bWriter.write("Alternatif Travel Total Cost: " + altRouteCost + "\n");
        bWriter.write("\n\n\t**********\t\n\n");
        
        bWriter.close();
        
        } catch (IOException ex) {
            Logger.getLogger(TravelingCargo.class.getName()).log(Level.SEVERE, null, ex);
        }


        // hangi map'in çizdirileceği seçiliyor
        int cho;
        TurkeyMap T2 = new TurkeyMap(sp.bestTravelPathList);
        TurkeyMap T = new TurkeyMap(sp.altTravelPathList);
        System.out.println("En iyi yolu gormek icin(1) "+
                "\nAlternatif yolu görmek icin(2)"+
                    "Cıkmak icin (0) basınız");
        System.out.println("not:\tHaritadan cıkmak icin lutfen back tusunu kullanın!!");
        
        do{
            System.out.print("Seciminiz : ");
            cho = scan.nextInt();
            if(cho == 0){
                T.setVisible(false);
                T2.setVisible(false);
                break;
            }
            else if(cho == 1){
                T.setVisible(false);
                T2.setVisible(true);
            }
            else if(cho == 2){
                T2.setVisible(false);
                T.setVisible(true);
            }
            else{
                T.setVisible(false);
                T2.setVisible(false);
                 System.out.println("Hatali secim!!!");   
                }
        
        
        }while(true);

        System.out.println("\tProgamı bitirdiniz!!!");
        
        System.exit(0);
        

    }
}

class ShortestPath {

    private static final boolean TIME_FLAG = false;

    int[][] adj_matrix; // komsuluk matrisi
    int totalTravelCost; // toplam harcanan yol

    ArrayList<String> cities; //sehirlerin isimleri

    City bestSolution; //Will change by routes

    List<City> solutionStack;
    List<List<City>> alternativeSolutions; 

    List<Integer> toVisitedCities; // ziyaret edilmesini istedigimiz sehirleri tutar

    List<List<Integer>> travelPathList;
    List<List<Integer>> bestTravelPathList; //Best Travel Path
    List<List<Integer>> altTravelPathList; //Alternative Travel Path List

    ShortestPath(List<Integer> _toVisitedCities) {
        totalTravelCost = 0;
        alternativeSolutions = new ArrayList<>();
        cities = new ArrayList<>();
        adj_matrix = new int[81][81];

        travelPathList = new ArrayList<>();
        bestTravelPathList = new ArrayList<>();
        altTravelPathList = new ArrayList<>();

        solutionStack = new ArrayList<>(); //Only backtrace
        toVisitedCities = _toVisitedCities;
    }

    public void run() {
        //bu method arama ıslemini yapmaya baslar 
        try {
            createAdjacencyMatrix();//komsuluk matrisini olusturur
        } catch (IOException ex) {
            Logger.getLogger(ShortestPath.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int i = 0; i < toVisitedCities.size() - 1; ++i) {
            int source = toVisitedCities.get(i);
            int destination = toVisitedCities.get(i + 1);

            solutionStack = new ArrayList<>(); // Clear
            bestSolution = new City(-1, -1, -1, Integer.MAX_VALUE);

            List<City> altSolList = process(source, destination, solutionStack, adj_matrix, 81); //Return top 5 solutions
            alternativeSolutions.add(altSolList); //Bulduğumuz en iyi 5 sonucu ekliyoruz

            System.out.println("Best Travel Cost: \t" + bestSolution.totalCost);
            System.out.print("Best Travel Path: \t");
            //en kısa yolları parca parca burada tutulur
            List<Integer> bestTravelPath = backTrace(solutionStack, bestSolution);
            bestTravelPathList.add(bestTravelPath); // yol bırlestırılır

            //Backtrace for alternative solutions
            for (int k = 0; k < altSolList.size(); ++k) {
                //alt yol parca parca alınır
                List<Integer> altTravelPath = backTrace(solutionStack, altSolList.get(k));
                altTravelPathList.add(altTravelPath); // alt yol birlestirilir
            }

            totalTravelCost += bestSolution.totalCost;//gidilen total mesafe hesaplanır
            
            
            for (int j = 0; j < altSolList.size(); ++j) {
                System.out.println("Alternative Travel Cost: \t" + altSolList.get(j).totalCost);
                System.out.print("Alternative  Travel Path: \t");

                List<Integer> travelPath = backTrace(solutionStack, altSolList.get(j));
                travelPathList.add(travelPath);
            }

            System.out.println("[Source]: " + source + "\t"
                    + "[Destination]: " + destination + "\t"
                    + "[Solution Size]: " + solutionStack.size() + "\t"
                    + "[Current Idx]: " + bestSolution.currentIdx + "\t"
                    + "[Parent Idx]: " + bestSolution.parentIdx + "\t"
                    + "[Parent Position]: " + bestSolution.parentPosition + "\t\t"
                    + "[Total Cost]: " + bestSolution.totalCost + "\n");
        }
        // best path'i bastırır
        System.out.print("Travel Best Path: ");
        for (int i = 0; i < bestTravelPathList.size(); ++i) {

            List<Integer> tmp = bestTravelPathList.get(i);

            for (int j = tmp.size() - 1; j > 0; --j) {
                System.out.print((tmp.get(j)+1) + " - ");
            }
        }
        
        System.out.println("41"); //Add to home

        System.out.println("Travel Total Cost: " + totalTravelCost + "\n");

        System.out.println("Alternative Route");

        int altRouteCost = 0;
        
        //alt yolu yazdırır
        for (int i = 0; i < altTravelPathList.size() / 2; ++i) {

            List<Integer> tmp = altTravelPathList.get(i * 2);

            int d = i;
            int m = 0;

            altRouteCost += alternativeSolutions.get(d).get(m).totalCost;

            for (int j = tmp.size() - 1; j > 0; --j) {
                System.out.print((tmp.get(j)+1) + " - ");
            }
        }

        System.out.println("41"); //Add to home

        System.out.println("Alternatif Travel Total Cost: " + altRouteCost + "\n");
    }

    public List<City> findNeigbours(int[][] adj_matrix, int size, int pIdx, int cPos, int cIdx, int rootCost) {
        // komsulukları bulur ve komsulukları komsuluk list'ine ekler
        List<City> neigbours = new ArrayList<>();
        for (int i = 0; i < size; ++i) {

            if (i == pIdx) {
                continue;
            }

            if (adj_matrix[cIdx][i] != 0) {
                neigbours.add(new City(cIdx, cPos, i, adj_matrix[cIdx][i] + rootCost));
            }
        }

        return neigbours;
    }

    public List<City> process(int source, int destination, List<City> solution, int[][] adj_matrix, int size) {
        // Asıl arama kısmı 
        boolean isFound = false;

        int index = 0;
        int visitedCount = 0;

        solution.add(new City(-1, -1, source, 0));

        List<City> alternativeSolutions = new ArrayList<>();//alternatif çozum

        while (!isFound) {

            List<City> neighbours = findNeigbours(adj_matrix, size, solution.get(index).parentIdx, index,
                    solution.get(index).currentIdx, solution.get(index).totalCost);

            for (int i = 0; i < neighbours.size(); ++i) {

                if (neighbours.get(i).currentIdx == destination) {

                    if (neighbours.get(i).totalCost < bestSolution.totalCost) {
                        bestSolution = neighbours.get(i);
                    }
                    //Add solution to list which returned
                    alternativeSolutions.add(neighbours.get(i));

                    visitedCount++;

                    if (visitedCount == 3) {
                        isFound = true;
                    }

                } else {
                    solution.add(neighbours.get(i));
                }
            }

            index++;
        }

        for (int i = 0; i < alternativeSolutions.size(); ++i) {
            if (bestSolution.currentIdx == alternativeSolutions.get(i).currentIdx
                    && bestSolution.totalCost == alternativeSolutions.get(i).totalCost) {
                alternativeSolutions.remove(i);
            }
        }

        return alternativeSolutions;
    }

    public List<Integer> backTrace(List<City> pSolution, City destination) {
        //bu method , city tipindeki konumdan surekli parentıd sini alarak yolu bulur
        boolean isFound = false;

        City prev; //parentcity tutacak
        City current = destination;

        System.out.print(destination.currentIdx + " - ");

        List<Integer> path = new ArrayList();// yolları tutuyor
        path.add(destination.currentIdx);

        while (!isFound) {
            prev = pSolution.get(current.parentPosition);

            if (prev.parentIdx == -1) {
                isFound = true;
            }

            System.out.print(current.parentIdx + " - ");
            path.add(current.parentIdx);

            current = prev;
        }

        System.out.println("\n");

        return path;
    }
    
    void createAdjacencyMatrix() throws FileNotFoundException, IOException {
        //bu method mesafeler.txt dosyanını okur ve komsuluk matrisini olusturur
        File f = new File("mesafeler.txt");
        if (f.exists()) {
            System.out.println("Dosya vardır.");
        } else {
            System.out.println("Dosya yoktur.");
        }

        FileReader fr;
        fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);

        for (int i = 0; i < 81; i++) {
            String yazi = br.readLine();
            String[] yazilar = yazi.split(",");

            for (int j = 0; j < 81; j++) {
                if (i == j) {
                    cities.add(yazilar[j]);
                    adj_matrix[i][j] = 0;
                } else {
                    adj_matrix[i][j] = Integer.parseInt(yazilar[j]);
                }
            }
        }
    }
}

class City { //sehirleri tutan srtuct

    int parentIdx; // suanki sehre geldigimiz sehri tutar
    int parentPosition;
    int currentIdx; // suan ki sehrin plakası
    int totalCost; // gidilen yol mesafelerini tutar

    City() {
        parentIdx = -1;
        currentIdx = -1;
        totalCost = 0;
        parentPosition = -1;
    }

    City(int _parentIdx, int _parentPosition, int _currentIdx, int _totalCost) {
        parentIdx = _parentIdx;
        parentPosition = _parentPosition;
        currentIdx = _currentIdx;
        totalCost = _totalCost;
    }

    public int getParentIdx() {
        return parentIdx;
    }

    public void setParentIdx(int parentIdx) {
        this.parentIdx = parentIdx;
    }

    public int getCurrentIdx() {
        return currentIdx;
    }

    public void setCurrentIdx(int currentIdx) {
        this.currentIdx = currentIdx;
    }

    public int getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(int totalCost) {
        this.totalCost = totalCost;
    }

    public int getParentPosition() {
        return parentPosition;
    }

    public void setParentPosition(int parentPosition) {
        this.parentPosition = parentPosition;
    }
}
