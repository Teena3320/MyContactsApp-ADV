package services;

import java.util.*;
import java.util.stream.Collectors;

public class ContactGroup implements GroupMember {

    private final String name;
    private final List<GroupMember> children = new ArrayList<>();

    public ContactGroup(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String name() { return name; }

    public boolean addChild(GroupMember member) {
        return children.add(member);
    }

    public boolean removeContact(UUID contactId) {
        boolean removed = false;
        Iterator<GroupMember> it = children.iterator();
        while (it.hasNext()) {
            GroupMember m = it.next();
            if (m instanceof ContactLeaf leaf && leaf.contactId().equals(contactId)) {
                it.remove();
                removed = true;
            }
        }
        for (GroupMember m : children) {
            if (m instanceof ContactGroup g) {
                removed |= g.removeContact(contactId);
            }
        }
        return removed;
    }

    @Override
    public Set<UUID> flattenContactIds() {
        return children.stream()
                .map(GroupMember::flattenContactIds)
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}