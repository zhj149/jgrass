/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) { 
 * HydroloGIS - www.hydrologis.com                                                   
 * C.U.D.A.M. - http://www.unitn.it/dipartimenti/cudam                               
 * The JGrass developer team - www.jgrass.org                                         
 * }
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.libs.utils;

public interface FluidConstants {
    final double pitnovalue = -1.0; // the novalue needed by PitFiller
    final double flownovalue = -1.0; // the novalue needed by Flow
    final double dirdrennovalue = -9999.0; // the novalue needed by DirDren
    final double slopenovalue = -9999.0; // the novalue needed by Slope
//    final int intnovalue = 9; // novalue integer
//    final double fluidnovalue = -9999.0; // the novalue as read from GRASS
//    final int fluidnovalueint = -9999; // the novalue as read from GRASS
//    final short fluidnovalueshort = -9999; // the novalue as read from GRASS

    final double Pi = 3.14159265358979; /* P greco */
    final double omega = 0.261799388; /* velocita' di rotazione terrestre [rad/h] */
    final double tk = 273.15; /* =0 C in Kelvin */
    final double ka = 0.41; /* costante di Von Karman */
    final double Tf = 0.0; /* freezing temperature [C] */
    final double Isc = 1367.0; /* Costante solare [W/m2] */
    final double rho_w = 1000.0; /* densita' dell'acqua [kg/m3] */
    final double rho_i = 917.0; /* densita' del ghiaccio [kg/m3] */
    final double Lf = 333700.00; /* calore latente di fusione [J/kg] */
    final double Lv = 2834000.00; /* calore latente di sublimazione [J/kg] */
    final double C_liq = 4188.00; /* heat capacity of water       [J/(kg/K)] */
    final double C_ice = 2117.27; /* heat capacity of ice     [J/(kg/K)] */
    final double GAMMA = 0.006509; /* adiabatic lapse rate [K/m]*/
    final double sigma = 5.67E-8; /* costante di Stefan-Boltzmann [W/(m2 K4)]*/
    
}
