<h1 align="center"> Figura </h1>
<p align="center">
  <img alt="fabric" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg">
  <img alt="forge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/forge_vector.svg">
  <img alt="quilt" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/quilt_vector.svg">
</p>

```diff
- Notice: The previous owner has deleted everything. We are currently rebuilding.
```

## A Minecraft Java client mod that allows you to extensively customize your player model and have other players see your Avatar without requiring any server mods!
![blockbench](https://github.com/Covkie/FiguraRME/assets/52550063/2f8bfff9-f5d6-46bd-a98c-7a9eed3faa7b)

## Utilizing the full potential of [Blockbench](https://www.blockbench.net/).
![animation](https://github.com/Covkie/FiguraRME/assets/52550063/d9e0e7b6-675f-40b3-a6fb-88c057ee50ab)

![mesh](https://github.com/Covkie/FiguraRME/assets/52550063/22742e4b-31fe-42cc-b8f4-0cedfe909a05)

## Not only can you customize your model but Figura also has an optional [Lua](https://www.lua.org/) API to make your own scripts!

![lua](https://github.com/Covkie/FiguraRME/assets/52550063/9201d481-1c52-42b5-813f-ca09a2fa5378)

## What if someone is invisible, or very small? Take advantage of Figura's robust permission system!

![perms](https://github.com/Covkie/FiguraRME/assets/52550063/92904d8d-3e38-4faf-b446-5f9480a8321f)

## We also have some extras, like:
![Qol](https://github.com/Covkie/FiguraRME/assets/52550063/d9db538e-d1b6-42eb-ad44-fbcc559c3c1f)

![skullemoji](https://github.com/Covkie/FiguraRME/assets/52550063/91c0f373-7048-4b2f-90db-e6891fa29589)

Meet us on the [FiguraMC Discord Server](https://discord.gg/figuramc) for more info and help :)

# Links
[discord]: https://discord.com/api/guilds/1129805506354085959/widget.png
[modrinth]: https://img.shields.io/badge/Modrinth-1bd96a?logo=modrinth&logoColor=ffffff&labelColor=1bd96a
[curseforge]: https://img.shields.io/badge/CurseForge-f16436?logo=curseforge&logoColor=ffffff&labelColor=f16436
[kofi]: https://img.shields.io/badge/Ko--fi-00b9fe?logo=kofi&logoColor=ffffff&labelColor=00b9fe
[collective]: https://img.shields.io/badge/Open%20Collective-83b3fb?logo=opencollective&logoColor=ffffff&labelColor=83b3fb
[wiki]: https://img.shields.io/badge/Figura-Wiki-black?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAAAXNSR0IArs4c6QAAAM5JREFUaIHtmbENAjEMRRPECIiSmvrYAZobggXoGIMhGOIa2IHUNDTU7HAsYBeWgh6W/isTx7knS5Z1qaUTwzTPkfg21trj3kWPJCQSoJEATbgTeN2mbYJ53vZ6tDulr4AEaCRAIwEaCdBIgMadO4aLM/PsnfjxELq4TTc7z92JP9szUvoKSIBGAjQSoJEAjQRoqjfzlK194HPc/fBzSlldH/bG015OXwEJ0EiAZhk94HWJXn+no6SvgARoJEBT16dX6IX930hfAQnQSIDmC5x4H/qkICDcAAAAAElFTkSuQmCC&labelColor=166ddd&color=5ea5ff

## Social: [ ![discord][] ](https://discord.gg/figuramc)

## Download: [ ![modrinth] ](https://modrinth.com/mod/figura) [ ![curseforge][] ](https://curseforge.com/minecraft/mc-mods/figura)

## Donate: [ ![collective][] ](https://opencollective.com/figura) [ ![kofi][] ](https://ko-fi.com/skyrina)

## Wiki: [ ![wiki][] ](https://wiki.figuramc.org/)

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

### • My emissives doesn't glow, nor have bloom with Iris/Optifine shaders?
> Since some shaders doesnt support emissives, a compatibility setting (default on) will change the render type of emissive textures to render them at it were fullbright, however that can lead to some unintended results
>
> You can force your avatar to use the correct emissive render type by using the render type `EYES` on your model

### • How can I use Figura with OptiFine?
> Figura will work with Optifine but due it's closed source nature issues might arise, therefore we still recommend you try using Sodium+Iris (Fabric) or Rubidium+Oculus (Forge) instead
> 
> Check out the full list of [alternatives](https://lambdaurora.dev/optifine_alternatives/)

### • Where can I find Avatars to download?
> For now you can find Avatars in the showcase channel in the official Discord server (A Web Based and In-Game browser is in the works!)

### • My Minecraft is cracked (non-premium/non-original) or I'm trying to join a cracked offline mode server, why can't I use Figura?
> Figura uses your account's UUID and your Mojang authentication as a way to prove you own that account, avoiding unwanted / malicious uploads
> 
> Non-premium Minecraft accounts don't authenticate with Mojang, and Offline mode servers don't report working UUID's, as such can neither upload nor download Figura avatars


## Community Resources

* Want to learn / get into Lua scripting?
  check out this [Lua quickstart](https://manuel-3.github.io/lua-quickstart) made by Manuel


* If you are tired of having to be in-game to look in the wiki, applejuice hosts the wiki as a [website](https://applejuiceyy.github.io/figs/)


* Are you new to Figura and are looking for a video tutorial about how everything works? You should probably watch Chloe's [Figura tutorial series](https://www.youtube.com/playlist?list=PLNz7v2g2SFA8lOQUDS4z4-gIDLi_dWAhl) on YouTube


* Do you wish there was another wiki for the rewrite?
  Slyme has an [unofficial wiki](https://slymeball.github.io/Figura-Wiki) covering most of Figura's basics


* Do you use VSCode and wish Figura's documentation autocompleted in the editor? GrandpaScout saves the day with their [VSDocs](https://github.com/GrandpaScout/FiguraRewriteVSDocs/wiki)
