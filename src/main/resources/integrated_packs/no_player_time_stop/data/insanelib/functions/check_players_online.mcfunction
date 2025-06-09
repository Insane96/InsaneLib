#execute if entity @a run gamerule doDaylightCycle_tc true
execute if entity @a run gamerule doWeatherCycle true
#execute unless entity @a run gamerule doDaylightCycle_tc false
execute unless entity @a run gamerule doWeatherCycle false
schedule function insanelib:check_players_online 1s