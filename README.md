# NOTE: This readme file will be replaced soon.

# Figura

## A fully clientside Minecraft Java mod that allows you to customize your player model!
![image](https://user-images.githubusercontent.com/40085587/232265398-f193b7c2-428f-41df-bc65-7c6b5a02e31b.png)

## Utilizing the full potential of [Blockbench](https://www.blockbench.net/), including:

![image](https://user-images.githubusercontent.com/40085587/232266646-72ea56aa-fb99-4706-bbac-dd242fbad57f.png)

![image](https://user-images.githubusercontent.com/40085587/232266641-0ef9d180-4190-46f5-a4a6-7e01e06fe6a1.png)

## But not only that, Figura also have a whole optional [Lua](https://www.lua.org/) API to make your own scripts!

![image](https://user-images.githubusercontent.com/40085587/232266708-9d6d9da6-a454-4ca0-83f2-a58c229ab03b.png)

## But hold on, thats too good, what if someone is invisible, or very small?

![image](https://user-images.githubusercontent.com/40085587/232266726-9d31c171-b16f-4246-adbe-30a337d7f15b.png)

*\*Figura do not take responsability of cheaty Avatars, all Avatars are responsibility of their creators*

## We also have some extras, like:

![image](https://user-images.githubusercontent.com/40085587/232266831-5266010b-28c8-4fa1-987c-daad23657bac.png)

Meet us on Discord for more info and help :)

# Links
[discord]: https://discord.com/api/guilds/1129805506354085959/widget.png
[modrinth]: https://img.shields.io/badge/Modrinth-1bd96a?logo=modrinth&logoColor=ffffff&labelColor=1bd96a
[curseforge]: https://img.shields.io/badge/CurseForge-f16436?logo=curseforge&logoColor=ffffff&labelColor=f16436
[kofi]: https://img.shields.io/badge/Ko--fi-00b9fe?logo=kofi&logoColor=ffffff&labelColor=00b9fe

## Social:
[ ![discord][] ](https://discord.gg/AsZmnapx)

## Download:
[ ![modrinth] ](https://modrinth.com/mod/figura)
[ ![curseforge][] ](https://curseforge.com/minecraft/mc-mods/figura)

## Donate:
[ ![kofi][] ](https://ko-fi.com/skyrina)

# FAQ

### • My avatars don't appear in the Figura list, even though they're in the correct folder?
> Check if your avatar has a file called "avatar.json" (don't forget to check file extensions)
> 
> This file can be completely empty, it just needs to be present for Figura to recognise it as an avatar

### • How do I hide the vanilla model?
> At the top of your script, put:
>
> • To hide literally everything (player, armor, elytra, held items):
> ```lua
> vanilla_model.ALL:setVisible(false)
> ```
>
> • To hide only the player:
> ```lua
> vanilla_model.PLAYER:setVisible(false)
> ```
>
> • To hide only armor:
> ```lua
> vanilla_model.ARMOR:setVisible(false)
> ```
> 
> • To hide other, or specific parts, you can check the in-game docs

### • How do I play a Blockbench Animation?
> Simply put this code in your script:
> ```lua
> animations.modelName.animationName:play()
> ```
> Where:
> 
> "`animations`" is the global table which contains all animations
> 
> "`modelName`" is the name of the model you are accessing the animation from
> 
> "`animationName`" is, as the name says, the animation name

### • What are Pings and how do I use them?
> Pings are Lua functions that are executed for everyone running your avatar's script
> 
> Pings are sent from the host player, and can be used to sync things like keypresses or action wheel actions
> 
> To create a ping:
> ```lua
> function pings.myPing(arg1, arg2)
>   -- code to run once the ping is called
> end
> ```
> And to execute the ping, it's as shrimple as calling a lua function:
> ```lua
> pings.myPing("Hello", "World")
> ```
> Note that pings are limited in their content and size, and are rate-limited

### • How can I add an emissive texture?
> Name the texture the same as the non-emissive counterpart, then add `_e` to the end
> 
> And don't forget to set the places you don't want to glow to **transparent black** (#00000000), to also ensure compatibility with shader mods

### • My emissives doesn't glow, nor have bloom with Iris shaders?
> Since some shaders doesnt support emissives, a compatibility setting (default on) will change the render type of emissive textures to render them at it were fullbright, however that can lead to some unintended results
>
> You can force your avatar to use the correct emissive render type by using the render type `EYES` on your model


### • Where can I find avatars to download?
> For now you can find avatars in the showcase channel in the official Discord server

### • My Minecraft is cracked (non-premium/non-original), why can't I use Figura?
> Figura uses your account's UUID and your Mojang authentication as a way to prove you own that account, avoiding unwanted / malicious uploads
> 
> Non-premium Minecraft accounts don't authenticate with Mojang, and as such can neither upload nor download Figura avatars

## Community Resources

* Want to learn / get into Lua scripting?
  check out this [Lua quickstart](https://manuel-3.github.io/lua-quickstart) made by Manuel


* If you are tired of having to be in-game to look in the wiki, applejuice hosts the wiki as a [website](https://applejuiceyy.github.io/figs/)


* Are you new to Figura and are looking for a video tutorial about how everything works? You should probably watch Chloe's [Figura tutorial series](https://www.youtube.com/playlist?list=PLNz7v2g2SFA8lOQUDS4z4-gIDLi_dWAhl) on YouTube


* Do you wish there was a wiki for the rewrite?
  Slyme has an [unofficial wiki](https://slymeball.github.io/Figura-Wiki) covering most of Figura's basics


* If you want a more in-depth wiki, with the GitHub style, Katt made one [here](https://github.com/KitCat962/FiguraRewriteRewrite/wiki) 


* Do you use VSCode and wish Figura's documentation autocompleted in the editor? GrandpaScout saves the day with their [VSDocs](https://github.com/GrandpaScout/FiguraRewriteVSDocs/wiki)
