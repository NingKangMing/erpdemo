package com.aioerp.common.plugin.port;

import com.aioerp.port.model.om.OmPortUser;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

public class AioerpOmPortPlugin
{
  public AioerpOmPortPlugin(ActiveRecordPlugin arp)
  {
    arp.addMapping("om_port_user", OmPortUser.class);
  }
}
