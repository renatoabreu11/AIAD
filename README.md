# AIAD
Repository to host Agents and Distributed Artificial Intelligence project.

AIAD is a course given at FEUP in the fourth year of the Master in Informatics and Computing Engineering.

## Goal

Implementar uma simulação baseada em agentes para estudar diferentes formas de gestão dos preços praticados pelos parques de
estacionamento.

## Description

A localização e utilização dos parques de estacionamento numa cidade tem um grande impacto na fluência do tráfego numa cidade. Pretende-se com este trabalho modelar o comportamento de diferentes tipos de condutores, que se deslocam para diferentes pontos numa cidade, e que têm necessidade de estacionar o automóvel num dos parques existentes. Cada condutor tem um ponto de partida e um ponto de destino (obtido a partir de uma distribuição normal nas imediações do centro da cidade), o tempo previsto de chegada, o valor máximo que está disposto a pagar por hora, a duração prevista do estacionamento, a distância máxima a percorrer a pé.

A medida de "utilidade" obtida por um condutor está relacionada com o preço a pagar e com a distância do estacionamento escolhido ao local de destino. Alguns condutores são "racionais", podendo assumir-se que conhecem os preços praticados em cada parque de estacionamento, e tomando a decisão do local a estacionar (se tiver lugares livres) de acordo com essa informação. Outros condutores são "exploradores", tendo menos conhecimento das opções de estacionamento existentes, e ponderando estacionar num parque que encontrem (de acordo com a medida de "utilidade").

Pretende-se igualmente estudar o impacto de diferentes estratégias de preços de estacionamento dos diferentes parques: preço fixo vs. dinâmico, em que o preço pode ser ajustado de acordo com a ocupação atual do parque. Algumas métricas que podem ser avaliadas por experimentação são o lucro do parque e o grau de bem-estar social (medido a partir das "utilidades" dos condutores) obtido.
