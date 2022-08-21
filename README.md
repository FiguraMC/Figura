# Figura

Figura is a Minecraft Java mod that allows you to change your player _extensively_

With **Blockbench** models and **Lua** scripting, Figura is capable of doing almost anything you have in mind

Figura works both Singleplayer and Multiplayer, we have a custom server where your avatar is saved, so anyone using the mod could also see it

But having other people might be a concern, if you think someone is cheating or an avatar is too complex for your framerate?
you can filter those people and tweak their avatar available resources using a system called **trust settings**

**Note:** All avatars rights are reserved to their respective creators

## Links

[Discord](https://discord.gg/ekHGHcH8Af)  
[Reddit](https://www.reddit.com/r/Figura)

[Modrinth](https://modrinth.com/mod/figura)  
[Curseforge](https://curseforge.com/minecraft/mc-mods/figura)  

[Ko-fi](https://ko-fi.com/francy_chan)

## FAQ

### • My avatars do not appear in Figura list even if its on the correct folder?
> Check if your avatar have a file called "avatar.json" (dont forget to check extensions)
> 
> The contents of the file can just be empty, as the presence of that files tells Figura that this folder is an avatar

### • How do I hide the vanilla model?
> In the top of your script file, put:
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
> • To hide only the armor:
> ```lua
> vanilla_model.ARMOR:setVisible(false)
> ```

### • How do I play a Blockbench Animation?
> Simply run this code on your script:
> ```lua
> animations.modelName.animationName:play()
> ```
> whereas:
> 
> "`animations`" is the global table which contains all animations
> 
> "`modelName`" is the name of which model you are accessing the animation
> 
> "`animationName`" is, as the name says, the animation name

### • What are Pings and how do I use them?
> Pings are lua functions that are sent through the backend to be executed in other people seeing your avatar, they are meant a way to sync data that only your client have
> 
> Some things, like your keybind presses or action wheel actions, they are not sent to other clients, so they have no idea if you have executed it or not, so pings are used to sent this information to them
> 
> An example of creating a ping is:
> ```lua
> function pings.myPing(arg1, arg2)
>   -- code to run once the ping is called
> end
> ```
> And to actually execute them, and send it to the backend, it's as shrimple as calling a lua function:
> ```lua
> pings.myPing("Hello", "World")
> ```
> Note that pings are limited in its contents, size, and how many you can sent per second

### • How can I add an emissive texture?
> Simple, just name the texture, the same as the non-emissive counterpart, but then adding `_e` in the end
> 
> Alternatively, still renaming the texture the same as its non-emissive however without the `_e`, in blockbench, you could set the texture render type to `emissive`

### • How can I use Figura with OptiFine?
> You can't, OptiFine closed source nature and invasive code are a really high difficulty for mod compatibility. Check those [alternatives](https://lambdaurora.dev/optifine_alternatives/) instead

### • Forge port?
> No, however if you want to develop it yourself, feel free to do so

### • Where can I find avatars to download?
> For now you can find avatars in the showcase channels on the official Discord server

### • My Minecraft is cracked (non-premium/non-original) and I cannot use Figura?
> Non-premium Minecraft accounts cannot upload nor dowload Figura avatars
> 
> Figura uses your account UUID and your Mojang authentication as a way to prove you own that account, avoiding unwanted / malicious uploads

### • My emissive texture looks weird?
> If youre using a custom shader (Iris), it is the shaders who controls how emissive works, however, if this still happens without shaders, try colouring the places you dont want to glow as **transparent black** (#00000000), this is due how Iris handles emissives which takes account the transparency and colour of the pixels

## Community Resources

* Wanna learn / get into Lua scripting?
  check out this [Lua quickstart](https://manuel-3.github.io/lua-quickstart) made by Manuel


* If you are tired of having to be in game to look in the wiki, 
  applejuice have the wiki as a [website](https://applejuiceyy.github.io/figs/)


* Are you new to Figura and is looking for a video tutorial about how... everything works?
  you should probably watch Chloe's [Figura tutorial series](https://www.youtube.com/playlist?list=PLNz7v2g2SFA8lOQUDS4z4-gIDLi_dWAhl) on Youtube