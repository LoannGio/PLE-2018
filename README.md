# PLE-2018
Projet de PLE 2018




1201

Dem3 -> découpe en grille le planisphère

Dem3 : N45N066.hgt -> N45N066 = le coin haut gauche de la case de la grille correspondante

hgt2latlon : Dem3 -> lat, long, alti (mètres)
* crée un tableau de 1201x1201
* traite le nom du fichier
* parcours ligne a ligne
* inverse l'ordre des bits
* multiplie ce qu'il faut

chaque dem3 décale de 1 en latitude et 0.001 en longitude
