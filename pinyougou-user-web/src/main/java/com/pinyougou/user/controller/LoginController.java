package com.pinyougou.user.controller;

import java.util.*;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

	@RequestMapping("/name")
	public Map showName(){
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		/*Map map=new HashMap();
		map.put("loginName", name);*/
		Map<Integer, String> map = new HashMap<Integer, String>();
		map.put(1, "a");
		 map.put(2, "b");
		map.put(3, "ab");
		map.put(4, "ab");
		map.put(4, "ab");
		Iterator<Map.Entry<Integer, String>> it = map.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry<Integer, String> next = it.next();
			System.out.println(next.getKey()+next.getValue());
		}

		Iterator<Integer> iterator = map.keySet().iterator();
		while (iterator.hasNext()){
			Integer next = iterator.next();
			System.out.println(map.get(next));
		}

		Collection<String> values = map.values();
		for (String value : values) {
			System.out.println(value);
		}
		return map;		
	}
	
}
