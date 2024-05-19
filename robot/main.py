#!/usr/bin/env pybricks-micropython

"""
Example LEGO® MINDSTORMS® EV3 Robot Educator Driving Base Program
-----------------------------------------------------------------

This program requires LEGO® EV3 MicroPython v2.0.
Download: https://education.lego.com/en-us/support/mindstorms-ev3/python-for-ev3

Building instructions can be found at:
https://education.lego.com/en-us/support/mindstorms-ev3/building-instructions#robot
"""

#!/usr/bin/env pybricks-micropython

from pybricks.ev3devices import Motor, ColorSensor, GyroSensor
from pybricks.parameters import Port, Direction, Stop
from pybricks.robotics import DriveBase
from pybricks.tools import wait, StopWatch
from collections import deque
from umqtt.robust import MQTTClient
import math

motor_izquierdo = Motor(Port.D)
motor_derecho = Motor(Port.A)
motor_pala = Motor(Port.B)

color_sensor = ColorSensor(Port.S4)

giroscopio = GyroSensor(Port.S1, Direction.COUNTERCLOCKWISE)

robot = DriveBase(motor_izquierdo, motor_derecho, wheel_diameter = 55.5, axle_track = 104)

celda_x, celda_y, angulo = 6, 0, 0

DRIVE_SPEED = 150
PALA_ARRIBA = 100
PALA_ABAJO = 0

MQTT_Broker = "192.168.0.100"

rojo = 31
verde = 54
azul = 20
lado = 10

map_Topic = "/map"
calculations_Topic = "/Grupo G/calculations"
odometry_Topic = "/Grupo G/odometry"
orders_Topic = "/Grupo G/orders"

highlightedPanels = ""

roads = {}

dic = {"00": [],
       "01": ["izquierda", "derecha"],
       "02": ["arriba", "abajo"],
       "03": ["arriba", "derecha"],
       "04": ["derecha", "abajo"],
       "05": ["abajo", "izquierda"],
       "06": ["izquierda", "arriba"],
       "07": ["izquierda", "arriba", "derecha"],
       "08": ["arriba", "derecha", "abajo"],
       "09": ["derecha", "abajo", "izquierda"],
       "10": ["abajo", "izquierda", "arriba"],
       "11": ["arriba", "derecha", "abajo", "izquierda"]}

ordersQueue = []

primera = True

def shortest_road(mapMatrix, start_row, start_col, end_row, end_col):
    rows, cols = 7, 5
    visited = [[False] * cols for _ in range(rows)]
    queue = deque([(start_row, start_col, 0, [])])

    while queue:
        row, col, distance, road = queue.popleft()
        visited[row][col] = True

        if row == end_row and col == end_col:
            return road + [(row, col)]

        for dr, dc in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
            new_row, new_col = row + dr, col + dc
            if 0 <= new_row < rows and 0 <= new_col < cols:
                if (dr == -1 and "arriba" in mapMatrix[row][col] and "abajo" in mapMatrix[new_row][new_col] and not visited[new_row][new_col]) or \
                    (dr == 1 and "abajo" in mapMatrix[row][col] and "arriba" in mapMatrix[new_row][new_col] and not visited[new_row][new_col]) or \
                    (dc == -1 and "izquierda" in mapMatrix[row][col] and "derecha" in mapMatrix[new_row][new_col] and not visited[new_row][new_col]) or \
                    (dc == 1 and "derecha" in mapMatrix[row][col] and "izquierda" in mapMatrix[new_row][new_col] and not visited[new_row][new_col]):
                    new_road = road + [(row, col)]
                    queue.append((new_row, new_col, distance + 1, new_road))
                    visited[new_row][new_col] = True

def getMovements(road):
    lista_movimientos = []
    for i in range(len(road) - 1):
        origen = road[i]
        destino = road[i + 1]
        if origen[0] > destino[0]:
            lista_movimientos.append("arriba")
        elif origen[0] < destino[0]:
            lista_movimientos.append("abajo")
        elif origen[1] > destino[1]:
            lista_movimientos.append("izquierda")
        else:
            lista_movimientos.append("derecha")
    nueva_lista_movimientos = []
    for i in range(len(lista_movimientos) - 1):
        origen = lista_movimientos[i]
        destino = lista_movimientos[i + 1]
        movimiento = None
        if (origen == "izquierda" and destino == "arriba") or (origen == "arriba" and destino == "derecha") or (origen == "derecha" and destino == "abajo") or (origen == "abajo" and destino == "izquierda"):
            movimiento = "giro derecha"
        elif (origen == "derecha" and destino == "arriba") or (origen == "arriba" and destino == "izquierda") or (origen == "izquierda" and destino == "abajo") or (origen == "abajo" and destino == "derecha"):
            movimiento = "giro izquierda"
        if movimiento:
            nueva_lista_movimientos.append(movimiento)
        else:
            nueva_lista_movimientos.append(origen)
    return nueva_lista_movimientos

def processMap(city):
    global highlightedPanels, roads, angulo
    mapImages = [city[i: i + 2] for i in range(0, len(city), 2)]
    mapMatrix = [[0] * 5 for _ in range(7)]
    k = 0
    for i in range(7):
        for j in range(5):
            if i == 6 and j == 0:
                if mapImages[k] == "01":
                    angulo = 0
                else:
                    angulo = 90
            mapMatrix[i][j] = dic[mapImages[k]]
            k += 1
    highlightedPanelsInteger = []
    for i in range(7):
        for j in range(5):
            if i != 6 or j != 0:
                cont = 0
                movements = mapMatrix[i][j]
                for mov in movements:
                    if mov == "derecha" and (j + 1) < 5:
                        if "izquierda" in mapMatrix[i][j + 1]:
                            cont += 1
                    elif mov == "arriba" and (i - 1) >= 0:
                        if "abajo" in mapMatrix[i - 1][j]:
                            cont += 1
                    elif mov == "izquierda" and (j - 1) >= 0:
                        if "derecha" in mapMatrix[i][j - 1]:
                            cont += 1
                    elif mov == "abajo" and (i + 1) < 7:
                        if "arriba" in mapMatrix[i + 1][j]:
                            cont += 1
                    if cont == 2:
                        break
                if cont == 1:
                    highlightedPanels = highlightedPanels + str(i) + str(j)
                    highlightedPanelsInteger.append((i, j))
    for orig in highlightedPanelsInteger:
        for destino in highlightedPanelsInteger:
            if orig != destino:
                road = shortest_road(mapMatrix, orig[0], orig[1], destino[0], destino[1]) 
                roads[((orig[0], orig[1]), (destino[0], destino[1]))] = (road, getMovements(road))
                reverse_road = road[:]
                road.pop(0)
                reverse_road.reverse()
                roads[((destino[0], destino[1]), (orig[0], orig[1]))] = (reverse_road, getMovements(reverse_road))
                reverse_road.pop(0)
    for orig in highlightedPanelsInteger:
        road = shortest_road(mapMatrix, orig[0], orig[1], 6, 0) 
        roads[((orig[0], orig[1]), (6, 0))] = (road, getMovements(road))
        reverse_road = road[:]
        road.pop(0)
        reverse_road.reverse()
        roads[((6, 0), (orig[0], orig[1]))] = (reverse_road, getMovements(reverse_road))
        reverse_road.pop(0)

def processOrder(order):
    global ordersQueue
    x1 = int(order[0])
    y1 = int(order[1])
    x2 = int(order[2])
    y2 = int(order[3])
    ordersQueue.append(((x1, y1), (x2, y2)))

def reiniciar_odometria(angulo):
    motor_izquierdo.reset_angle(angulo)
    motor_derecho.reset_angle(angulo)
    giroscopio.reset_angle(angulo)

def getmessages(topic, msg):
    global primera
    topic = topic.decode()
    try:
        content = msg.decode()
        if primera and topic == map_Topic:
            primera = False
            processMap(content)
            reiniciar_odometria(angulo)
            client.publish(calculations_Topic, highlightedPanels)
        elif not primera and topic == map_Topic:
            client.publish(calculations_Topic, highlightedPanels)
        elif topic == orders_Topic:
            processOrder(content)
    except Exception as ex:
        print(ex)

def enviar_datos(celda_x, celda_y, angulo):
    data = str(celda_x) + str(celda_y) + ";" + str(angulo)
    client.publish(odometry_Topic, data)

def subir_pala():
    motor_pala.run_target(500, PALA_ARRIBA, then = Stop.HOLD, wait = True)

def bajar_pala():
    motor_pala.run_target(500, PALA_ABAJO, then = Stop.HOLD, wait = True)

def seguirVerde(color_actual, rojo, verde, azul, lado):
    if color_actual[0] >= (rojo - (lado/2)) and color_actual[0] <= (rojo + (lado/2)) and color_actual[1] >= (verde - (lado/2)) and color_actual[1] <= (verde + (lado/2)) and color_actual[2] >= (azul - (lado/2)) and color_actual[2] <= (azul + (lado/2)):
        turn_rate = -23
        robot.drive(DRIVE_SPEED, turn_rate)
    else:
        turn_rate = 23
        robot.drive(DRIVE_SPEED, turn_rate)

def salirDeNegro(primerNegro, lista_movimientos):
    turn_rate = 0
    drive_speed = DRIVE_SPEED
    if primerNegro:
        movimiento = lista_movimientos[1][0]
        if movimiento == "giro derecha":
            drive_speed = 150
            turn_rate = -65
        elif movimiento == "giro izquierda":
            drive_speed = 150
            turn_rate = 65
        else:
            turn_rate = -20
    else:
        turn_rate = -40
    robot.drive(drive_speed, turn_rate)       

def estaEnNegro(color_actual):
    return color_actual[0] >= 0 and color_actual[1] <= 25 and color_actual[1] >= 0 and color_actual[1] <= 25 and color_actual[2] >= 0 and color_actual[2] <= 25

def estoyDondePedido(pedidoActual, celda_x, celda_y):
    return celda_x == pedidoActual[0][0] and celda_y == pedidoActual[0][1]

def vuelta():
    robot.turn(-226)

def aplicarMovimiento(lista_movimientos, rojo, verde, azul, color_actual):
    movimiento = lista_movimientos[1][0]
    if movimiento in ["giro derecha", "giro izquierda"]:
        turn_rate = -80 if movimiento == "giro derecha" else 75
        lista_movimientos[1].pop(0)
        robot.turn(turn_rate)
        return True
    lista_movimientos[1].pop(0)
    seguirVerde(color_actual, rojo, verde, azul, lado)
    return True

def recogerPedido(vueltaInicial, celda_x, celda_y, lista_movimientos, primerNegro, seguirLinea, rojo, verde, azul, lado):
    pedidoRecogido = False
    color_actual = color_sensor.rgb()
    if not vueltaInicial:
        vuelta()
        vueltaInicial = True
    else:
        negro = estaEnNegro(color_actual)

        if negro:
            if primerNegro and (lista_movimientos == None or len(lista_movimientos[1]) == 0):
                bajar_pala()
                robot.stop()
                pedidoRecogido = True
            else:
                salirDeNegro(primerNegro, lista_movimientos)
                seguirLinea = False
        else:
            if primerNegro and not seguirLinea:
                movimiento_completado = aplicarMovimiento(lista_movimientos, rojo, verde, azul, color_actual)   
                if movimiento_completado:  
                    print("fuera de primer negro")
                    seguirLinea = True
                    primerNegro = False
            else:
                if not seguirLinea:
                    print("fuera de la linea negra")
                    posicion = lista_movimientos[0].pop(0)
                    celda_x, celda_y = posicion
                    primerNegro = True
                    seguirLinea = True
                seguirVerde(color_actual, rojo, verde, azul, lado)
            
    return vueltaInicial, celda_x, celda_y, primerNegro, seguirLinea, pedidoRecogido

def entregarPedido(vueltaInicial, celda_x, celda_y, lista_movimientos, primerNegro, seguirLinea, rojo, verde, azul, lado):
    pedidoEntregado = False
    color_actual = color_sensor.rgb()
    if not vueltaInicial:
        vuelta()
        vueltaInicial = True
    else:
        negro = estaEnNegro(color_actual)

        if negro:
            if primerNegro and len(lista_movimientos[1]) == 0:
                subir_pala()
                pedidoEntregado = True
            else:
                salirDeNegro(primerNegro, lista_movimientos)
                seguirLinea = False
        else:
            if primerNegro and not seguirLinea:
                movimiento_completado = aplicarMovimiento(lista_movimientos, rojo, verde, azul, color_actual)   
                if movimiento_completado:  
                    seguirLinea = True
                    primerNegro = False
            else:
                if not seguirLinea:
                    posicion = lista_movimientos[0].pop(0)            
                    celda_x, celda_y = posicion
                    primerNegro = True
                    seguirLinea = True
            seguirVerde(color_actual, rojo, verde, azul, lado)

    return vueltaInicial, celda_x, celda_y, primerNegro, seguirLinea, pedidoEntregado

def copiarLista(listaA):
    listaB = []
    for elemento in listaA:
        listaB.append(elemento)
    return listaB

try:
    # while True:
    #     color = color_sensor.rgb()
    #     print(color)

    subir_pala()

    client = MQTTClient("Robot", MQTT_Broker)
    client.connect()
    client.set_callback(getmessages)
    client.subscribe(map_Topic)
    client.subscribe(orders_Topic)

    primerNegro = False
    seguirLinea = True
    pedidoTomado = False
    pedidoRecogido = False
    pedidoEntregado = False
    pedidoActual = None
    caminoBusqueda = False
    caminoEntrega = False
    vueltaInicial = True
    lista_movimientos = None

    reloj = StopWatch()

    while True:  
        client.check_msg()  

        if reloj.time() >= 1000:
            enviar_datos(celda_x, celda_y, giroscopio.angle())
            reloj.reset()

        if len(ordersQueue) != 0 and not pedidoTomado:
            pedidoTomado = True
            pedidoActual = ordersQueue.pop(0) 

        if pedidoActual:
            if not pedidoRecogido:
                buscarPedido = estoyDondePedido(pedidoActual, celda_x, celda_y)
                if not buscarPedido or not pedidoRecogido:
                    if not caminoBusqueda:
                        if ((celda_x, celda_y), (pedidoActual[0][0], pedidoActual[0][1])) in roads:
                            lista_movimientos = roads[((celda_x, celda_y), (pedidoActual[0][0], pedidoActual[0][1]))] 
                        else:
                            lista_movimientos = None
                            vueltaInicial = True
                            primerNegro = True
                        caminoBusqueda = True
                    vueltaInicial, celda_x, celda_y, primerNegro, seguirLinea, pedidoRecogido = recogerPedido(vueltaInicial, celda_x, celda_y, lista_movimientos, primerNegro, seguirLinea, rojo, verde, azul, lado)
            else:
                if not pedidoEntregado:
                    if not caminoEntrega:
                        lista_movimientos = roads[((pedidoActual[0][0], pedidoActual[0][1]), (pedidoActual[1][0], pedidoActual[1][1]))]
                        caminoEntrega = True
                        pedidoRecogido = True
                        vueltaInicial = False
                        primerNegro = False
                        seguirLinea = True
                    vueltaInicial, celda_x, celda_y, primerNegro, seguirLinea, pedidoEntregado = entregarPedido(vueltaInicial, celda_x, celda_y, lista_movimientos, primerNegro, seguirLinea, rojo, verde, azul, lado)
                else:
                    primerNegro = False
                    seguirLinea = True
                    pedidoTomado = False
                    pedidoRecogido = False
                    pedidoEntregado = False
                    pedidoActual = None
                    caminoBusqueda = False
                    caminoEntrega = False
                    lista_movimientos = None
                    vueltaInicial = False
        else:
            robot.stop()

except Exception as ex:
    print(str(ex))
finally:
    robot.stop(Stop.BRAKE)