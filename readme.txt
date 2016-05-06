En el fichero de configuracion de MI router.

    A.B.C.D[:puerto] Se refiere a los ROUTERS que están conectados a mí.
        (Vecinos a los que les voy a mandar mi tabla con las subredes a las que llego).

    E.F.G.H/len Se refiere a las SUBREDES que tengo DIRECTAMENTE conectadas (dist 1).
        Son las que incorporo a mi tabla con la forma:
        [ HACIA E.F.G.H/len | DISTANCIA 1 | POR MÍ ].

La tabla de encaminamiento es de la forma:

    [    HACIA SUBRED   |     DISTANCIA     |       POR ROUTER      ]
    [ HACIA Q.W.E.R/32  |   DISTANCIA y     |   POR A.S.D.F[:5678]  ]
    [ HACIA Q.W.E.R/32  |   DISTANCIA z     |   POR A.S.D.F[:5678]  ]
    [ HACIA Q.W.E.R/32  |   DISTANCIA w     |   POR A.S.D.F[:5678]  ]

Los mensajes que mando a mis routers vecinos son:
    [    HACIA SUBRED   |     DISTANCIA     ]
        Como se lo mando yo, el router ya sabe que es POR MÍ.
